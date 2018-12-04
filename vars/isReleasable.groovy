/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */

def call(String version) {
    return isReleaseBranch(version) && isSnapshotVersion(version)
}
