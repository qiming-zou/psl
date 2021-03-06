#!/bin/bash

# Build and deploy the API docs for PSL.
# The docs are only deployed for tag pushes.

THIS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
REPO_ROOT_DIR="${THIS_DIR}/.."
DOCS_DIR="${REPO_ROOT_DIR}/target/site/apidocs"
TEMP_DIR='/tmp/psl-docs-deploy'

WEBSITE_REPO_URL="https://linqs-deploy:${LINQS_DEPLOY}@github.com/linqs/psl-website.git"

VERSION_TAG_REGEX='^(CANARY-)?[0-9]+\.[0-9]+\.[0-9]+$'
API_DIR='api'

function buildDocs() {
    echo "Building docs."

    pushd . > /dev/null
    cd "${REPO_ROOT_DIR}"
        mvn javadoc:aggregate
    popd > /dev/null
}

function deployDocs() {
    # Bail if no deploy keys exist.
    if [[ -z "${LINQS_DEPLOY}" ]]; then
        echo "Skipping docs deploy, cannot find key."
        return
    fi

    # Don't deploy pull requests.
    if [[ "${TRAVIS_PULL_REQUEST}" != 'false' ]]; then
        echo "Skipping docs deploy, no deploy on pull requests."
        return
    fi

    # Only deploy docs from known repos.
    if [[ "${TRAVIS_REPO_SLUG}" != 'linqs/psl' && "${TRAVIS_REPO_SLUG}" != 'eriq-augustine/psl' ]]; then
        echo "Skipping docs deploy, only deploy from known repos."
        return
    fi

    # Only match tags that look like versions.
    if [[ ! "${TRAVIS_TAG}" =~ $VERSION_TAG_REGEX ]]; then
        echo "Skipping docs deploy, only deploy on tags."
        return
    fi

    echo "Deploying docs (${TRAVIS_TAG})."

    rm -Rf "${TEMP_DIR}"
    mkdir -p "${TEMP_DIR}"

    pushd . > /dev/null
    cd "${TEMP_DIR}"
        git clone "${WEBSITE_REPO_URL}"
        cd psl-website

        mkdir -p "${API_DIR}"
        cp -r "${DOCS_DIR}" "${API_DIR}/${TRAVIS_TAG}"

        _scripts/update-versions.py

        git add .
        git commit -m "Added autogenerated api docs from CI (${TRAVIS_TAG})."
        git push
    popd > /dev/null
}

function main() {
    trap exit SIGINT
    set -e

    buildDocs
    deployDocs
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]] ; then
    main "$@"
fi
