#cloud-config
disk_setup:
  /dev/disk/azure/scsi1/lun0:
      table_type: gpt
      layout: True
      overwrite: True
fs_setup:
    - device: /dev/disk/azure/scsi1/lun0
      partition: 1
      filesystem: ext4
mounts:
    - ["/dev/disk/azure/scsi1/lun0-part1", "/var/jenkins_home", auto, "defaults,noexec,nofail"]

write_files:
  - path: /etc/systemd/system/jenkins.service
    content: |
      [Unit]
      Description=OpenEnclave Jenkins
      Requires=docker.service
      After=docker.service

      [Service]
      Type=simple
      Restart=always
      TimeoutStartSec=60
      ExecStartPre=/usr/bin/docker pull jenkins/jenkins:lts
      ExecStartPre=-/usr/bin/docker rm -f %p
      ExecStart=/usr/bin/docker run \
        --name %p \
        -v /var/run/docker.sock:/var/run/docker.sock \
        -v /var/jenkins_home:/var/jenkins_home  \
        -e JAVA_OPTS="-Djava.awt.headless=true -Dmail.smtp.starttls.enable=true" \
        --user root -p 8080:8080 -p 50000:50000 \
        jenkins/jenkins:lts
      ExecStop=/usr/bin/docker stop %p

      [Install]
      WantedBy=multi-user.target
  - path: /etc/nginx/sites-available/jenkins
    content: |
      server {
          listen 80;
          return 301 https://$host$request_uri;
      }

      server {

          listen 443;
          server_name ${jenkins_master_dns}.${location}.cloudapp.azure.com;
          ssl_certificate /etc/letsencrypt/live/${jenkins_master_dns}.${location}.cloudapp.azure.com/fullchain.pem; # managed by Certbot
          ssl_certificate_key /etc/letsencrypt/live/${jenkins_master_dns}.${location}.cloudapp.azure.com/privkey.pem; # managed by Certbot

          ssl on;
          ssl_session_cache  builtin:1000  shared:SSL:10m;
          ssl_protocols  TLSv1.2;
          ssl_prefer_server_ciphers on;
          ssl_ciphers ECDH+AESGCM:ECDH+CHACHA20:ECDH+AES256:ECDH+AES128:!aNULL:!SHA1:!AESCCM;

          # HSTS
          add_header Strict-Transport-Security "max-age=63072000" always;

          # OCSP stapling
          ssl_stapling on;
          ssl_stapling_verify on;

          access_log            /var/log/nginx/jenkins.access.log;

          location / {

            proxy_set_header        Host $host;
            proxy_set_header        X-Real-IP $remote_addr;
            proxy_set_header        X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header        X-Forwarded-Proto $scheme;

            # Fix the “It appears that your reverse proxy set up is broken" error.
            proxy_pass          http://localhost:8080;
            proxy_read_timeout  90;

            proxy_redirect      http://localhost:8080 https://${jenkins_master_dns}.${location}.cloudapp.azure.com;
          }
      }


apt:
  preserve_sources_list: true
  sources:
    certbot:
      source: "ppa:certbot/certbot"
    docker:
      source: deb [arch=amd64] https://download.docker.com/linux/ubuntu $RELEASE stable
      keyid: 9DC8 5822 9FC7 DD38 854A  E2D8 8D81 803C 0EBF CD88
package_update: true
package_upgrade: true
package_reboot_if_required: true
packages:
  - docker-ce
  - default-jre
  - git
  - apt-transport-https
  - ca-certificates
  - curl
  - software-properties-common
  - nginx
  - python-certbot-nginx
  - unzip

runcmd:
  - [ systemctl, daemon-reload ]
  - [ systemctl, enable, nginx.service ]
  - [ systemctl, start, nginx.service ]
  - [ systemctl, enable, jenkins.service ]
  - [ systemctl, start, jenkins.service ]
  - [ certbot, --nginx, -d, ${jenkins_master_dns}.${location}.cloudapp.azure.com, --non-interactive, --agree-tos, -m, oeciteam@microsoft.com  ]
  - [ ln, -sfn, /etc/nginx/sites-available/jenkins, /etc/nginx/sites-available/default ]
  - [ systemctl, restart, nginx.service ]

final_message: "Jenkins Master is finally up, after $UPTIME seconds"
