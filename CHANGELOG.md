# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [1.4.2] - 2018-11-02
### Fixed
- gitlabCommitStatus wants named parameters

## [1.4.1] - 2018-09-20
### Fixed
- notify of own build

## [1.4.0] - 2018-09-20
### Changed
- notifyBuild sends notification to slack instead of hipchat

## [1.3.0] - 2018-09-19
### Changed
- a branch name that matches \d+\.\d+ cannot be a feature branch
- a release branch is master or is a prefix to the project's version

## [1.2.2] - 2018-07-24
### Fixed
- a variable set on a node is not available in a parallel node.

## [1.2.1] - 2018-07-24
### Fixed
- maven release build must skip tests

## [1.2.0] - 2018-07-24
### Changed
- upon release of a maven project, job's display name show the released version

## [1.1.2] - 2018-07-24
### Fixed
- jenkinsfile imports itself as the library

## [1.1.1] - 2018-07-23
### Fixed
- jenkins displays both job number and released version number
- buildInfo is never pushed for sonar builds, simplify assigning the version

## [1.1.0] - 2018-07-20
### Added
- buildWithMaven now uses maven version 3.5.3
- step readChangelog returns most recent version and date

## [1.0.1] - 2018-06-19
### Fixed
- bypass _artifactory.publishBuildInfo_

## [1.0.0] - 2018-06-12

### Added
- lint, build and publish helm charts during maven builds

### Changed
- adhere to keepachangelog, use the most recent entry to label the release
- feature branch version to match /\d+\.\d+\.\d+(:?-.+)?-\p{Upper}+-\d+(:?-SNAPSHOT)?/

### Deprecated

### Removed

### Fixed
- during maven build, jenkins was trying to set the status of the jenkinsfile_library instead of the repo being build.
