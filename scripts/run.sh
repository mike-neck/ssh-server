#!/usr/bin/env bash

set -e

readonly defaultSshServerPath="/opt/ssh-server"

declare sshServerPath="${1:-""}"
if [[ -z "${sshServerPath}" && -d "${defaultSshServerPath}" ]]; then
  sshServerPath="${defaultSshServerPath}"
elif [[ ! -d "${sshServerPath}" ]]; then
  echo "the path to ssh-server(i.e. /opt/ssh-server) does not point to any existing directory[${sshServerPath}]" >> /dev/stderr
  exit 1
fi

readonly sshHostKey="${SSH_HOST_KEY:-"${sshServerPath}/ssh-keys/etc/ssh/ssh_host_ed25519_key"}"
readonly sshAuthorizedKeys="${SSH_AUTHORIZED_KEYS:-"${sshServerPath}/ssh-keys/server/authorized_keys"}"
readonly sshUser="${SSH_USERNAME:-"user"}"
readonly sshArtifactName="${SSH_ARTIFACT_NAME:-"ssh-server.tar"}"
readonly sshServerName="${sshArtifactName%.*}"
readonly sshPort="${SSH_PORT:-"2020"}"

if [[ ! -f "${sshHostKey}" ]]; then
  echo "sshHostKey not found @ [${sshHostKey}]" >> /dev/stderr
  exit 1
fi
if [[ ! -f "${sshAuthorizedKeys}" ]]; then
  echo "sshAuthorizedKeys not found @ [${sshAuthorizedKeys}]" >> /dev/stderr
  exit 1
fi
if [[ ! -f "${sshServerPath}/${sshArtifactName}" ]]; then
  echo "sshArtifact not found @ [${sshServerPath}/${sshArtifactName}]" >> /dev/stderr
  exit 1
fi

if [[ -d "${HOME}/.sdkman" ]]; then
  sdk u java "$(
  sdk l java  |
   grep -E '(local|install)' |
    grep '|' | grep -v 'mandrel' |
     grep '21'  |
      sed -e 's|>||g' -e 's/|//g' -e 's|only||g' |
       tr -s ' ' | cut -d' ' -f5
  )"
fi

readonly executablePath="${sshServerPath}/${sshServerName}/bin/${sshServerName}"
if [[ ! -f "${executablePath}" ]]; then
  tar xf "${sshServerPath}/${sshArtifactName}" -C "${sshServerPath}"
fi

if [[ ! -x "${executablePath}" ]]; then
  echo "ssh-server executable not found @ [${executablePath}]" >> /dev/stderr
  exit 1
fi

SSH_PORT=${sshPort} \
SSH_HOST_KEY="${sshHostKey}" \
SSH_AUTHORIZED_KEYS="${sshAuthorizedKeys}" \
SSH_USERNAME="${sshUser}" \
"${executablePath}"
