if (!process.env.GITHUB_AUTH || !process.env.GITHUB_ISSUE) {
    throw "REQUIRED ENV: GITHUB_AUTH, GITHUB_ISSUE"
}

const target = process.env.TARGET_RUNS || 5
    shouldStart = process.env.SHOULD_START || false,
    githubAuth = process.env.GITHUB_AUTH,
    githubIssue = process.env.GITHUB_ISSUE,
    pollInterval = 60*1000,
    staleBorsRequest = 4,
    dateFormat = "YYYY/MM/DD HH:mm:ss";

const { Octokit } = require("@octokit/rest"),
    moment = require("moment"),
    github = new Octokit({
        auth: githubAuth
    });

const githubActions = {
    monitorStart: "===== Bors Monitoring Start =====",
    monitorEnd: "===== Bors Monitoring End =====",
    borsTry: "bors try"
}

const writeComment = (comment) => {
    const body = githubActions[comment]
    console.log(`[Bors Monitor] Commenting: ${body}`);
    github.issues.createComment({
        owner: "openenclave",
        repo: "openenclave",
        issue_number: githubIssue,
        body
    });
}

const monitor = async (targetRuns = 5, createNew = false) => {

    try {

        const data = [],
            actions = [];
        let page = 1;
        while(1) {
            const res = await github.issues.listComments({
                owner: "openenclave",
                repo: "openenclave",
                issue_number: githubIssue,
                page
            });
            if (res.data.length > 0) {
                data.push(...res.data);
            } else {
                break;
            }
            page++;
        }

        let start = 0,
            streak = 0,
            end = 0,
            allRuns = [],
            previousRequests = [],
            successfulRuns = [],
            failedRuns = [];

        const reset = () => {
            streak = 0;
            allRuns = [];
            previousRequests = [];
            successfulRuns = [];
            failedRuns = [];
        }
        const comments = data.map((comment) => {

            const { body, user, created_at } = comment,
                { monitorStart, monitorEnd, borsTry } = githubActions,
                text = body.trim(),
                commentDate = moment(created_at);

            if (text === monitorStart) {
                start++;
            } else if (text === monitorEnd) {
                end++;
                if (start === end) {
                    reset();
                } 
            }

            if (start > end) {
                if (text === borsTry) {
                    previousRequests.push(commentDate);
                } else if (text === "bors try-" && previousRequests.length > 0) {
                    previousRequests.shift();
                } else if (user.login === "bors[bot]") {

                    if (text.includes("Already running a review")) {
                        previousRequests.shift();
                    } else if (previousRequests.length > 0) {

                        const prev = previousRequests.shift(),
                            duration = commentDate.diff(prev,"minutes"),
                            runDetails = {
                                start: prev.format(dateFormat),
                                end: commentDate.format(dateFormat),
                                duration
                            };

                        if (text.includes("Build succeeded")) {
                            runDetails.result = "success";
                            successfulRuns.push(runDetails);
                            allRuns.push(runDetails);
                            streak = streak > 0 ? streak+1 : 1;
                        } else if (text.includes("Build failed")) {
                            runDetails.result = "failed";
                            failedRuns.push(runDetails);
                            allRuns.push(runDetails);
                            streak = streak < 0 ? streak-1 : -1;
                        } else {
                            previousRequests.unshift(prev);
                        }
                    }
                }
            }

            return comment;
        })

        if (start === end) {
            if (start === 0 || createNew) {
                actions.push("monitorStart");
                start++;
                successfulRuns = [];
            } else {
                console.log("\tMonitoring exiting - SHOULD_START=false");
                return;
            }
        }

        const pending = previousRequests.length,
            totalRuns = allRuns.length,
            success = successfulRuns.length,
            failed = failedRuns.length,
            isCycleRunning = start - end > 0,
            noCurrentRuns = pending === 0;

        //console.log(isCycleRunning, noCurrentRuns, success, failed, targetRuns, pending, totalRuns)
        if (isCycleRunning && noCurrentRuns && streak < targetRuns) {
            actions.push("borsTry");
        }

        const monitoringComplete = streak === targetRuns,
            requestDate = moment(),
            monitorStatus = {
                date: requestDate.format(dateFormat),
                issue: githubIssue,
                target: targetRuns,
                streak,
                pending: previousRequests.map((prev) => {return prev.format(dateFormat)}),
                sucess: successfulRuns,
                failed: failedRuns,
                runs: allRuns,
                completedCycles: end,
                actions,
                monitoringComplete
            }

        if (pending - totalRuns > 0 && previousRequests.length > 0) {
            const lastRequestDate = new moment(previousRequests[0])

            monitorStatus["lastRequestDate"] = lastRequestDate.format(dateFormat);
            monitorStatus["sinceLastRequest"] = requestDate.diff(lastRequestDate,"minutes");
        }

        console.log(monitorStatus)

        if (actions.length > 0) {
            actions.forEach((action) => {
                writeComment(action);
            });
        }

        if (monitoringComplete) {
            console.log(`\tMonitoring complete - ${success}/${totalRuns} runs succeeded`);
            writeComment(monitorEnd);
            return;
        }

        setTimeout(() => {monitor(targetRuns)}, pollInterval);

    } catch(e) {
        console.log("error:",e);
    }

}

monitor(target,shouldStart);
