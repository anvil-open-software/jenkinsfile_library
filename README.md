# jenkinsfile library


A collection of variables to help build projects with a Jekinsfile.

The variable `buildWithMaven` implements a complete pipeline that performs maven builds, sonar analysis and release.

```
@Library('jenkinsfile_library@DLABS-1381') _

buildWithMaven()
```

The variables can also be used individually. Simply imports the library.

Every commit to master is automatically tagged by a Jenkins jobs. Only use a tagged version of the library.

# For Curious Users
The library must be defined on the [Jenkins configuration page](http://jenkins.gcp.dematiclabs.com/configure), under _Global Pipeline Libraries_ to be accessible.

# Reference
+ [TN-027 Continuous Delivery at DLabs](https://wiki.dematic.com/display/PTSL/TN-027+Continuous+Delivery+at+DLabs)
for background information about the versioning concepts.
+ [Extending with Shared Libraries](https://jenkins.io/doc/book/pipeline/shared-libraries/)