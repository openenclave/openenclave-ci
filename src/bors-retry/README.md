Bors Retry Monitor
=================

Instructions
------------

Monitors GitHub pull requests, initiating Bors builds until the desired number of consecutive successful runs (TARGET_RUNS) has been met.

1. Build the docker container:
```
docker build -t bors-retry
```

2. Run the docker container with the appropriate environment variables:
```
docker run \
    -e TARGET_RUNS <number, default: 5> \
    -e SHOULD_START <boolean, default: false> \
    -e GITHUB_AUTH <string, *required*> \
    -e GITHUB_ISSUE <number, *required*> \
    bors-retry
```

The monitor could also be ran as a NodeJS app by setting the appropriate environment variables, and the command `npm start`.

Required Arguments
-----------------

GITHUB_AUTH - Personal GitHub access token [(tutorial)](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line)

GITHUB_ISSUE - Issue number to monitor

Optional Arguments
-----------------
TARGET_RUNS - Target # of consecutive successful runs (default: 5)

SHOULD_START - Start a new monitoring cycle, if previous one is already finished (default: false)

Monitoring Output
----------------
```
{
  date: <[string] timestamp of the monitoring sample>,
  issue: <[number] GitHub PR being monitored>,
  target: <[number] corresponds with the TARGET_RUNS env variable>,
  streak: <[number] number of consecutive success (streak > 0) or failures (streak < 0)>
  pending: <[array - string] date of the latest `bors try` request>,
  success: <[array - run information] all successful Bors runs>,
  failed: <array - run information] all failed Bors runs>,
  runs: <[array - run information] all Bors runs>,
  completedCycles: <[number] number of completed Bors cycles (a cycle is reaching the targetted number of runs)>,
  actions: <[array - string] pending actions for a given monitoring sample>,
  monitoringComplete: <[boolean] end of a cycle detected>
}
```

Run Information:
```
{
  start: <[string] timestamp of Bors try request>,
  end: <[string] timestamp of completion of Bors try>,
  duration: <[number] duration of Bors build in minutes>,
  result: <[string] result of the build: 'success' or 'failed'>
}
```

Actions:
```
monitorStart - initiating a cycle
borsTry - initiating a Bors run
```

Sample monitoring output:
```
{
  date: '2020/05/31 06:40:35',
  issue: '3083',
  target: '5',
  pending: [ '2020/05/31 06:11:39' ],
  sucess: [
    {
      start: '2020/05/30 05:41:09',
      end: '2020/05/30 06:18:08',
      duration: 36,
      result: 'success'
    },
    ...
  ],
  failed: [
    {
      start: '2020/05/30 02:21:29',
      end: '2020/05/30 03:41:39',
      duration: 80,
      result: 'failed'
    },
    ...
  ],
  runs: [
    {
      start: '2020/05/30 02:21:29',
      end: '2020/05/30 03:41:39',
      duration: 80,
      result: 'failed'
    },
    ...
  ],
  completedCycles: 2,
  actions: [],
  monitoringComplete: false
}
```
