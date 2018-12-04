/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */

def call(String version) {
     return env.BRANCH_NAME == 'master' || (env.BRANCH_NAME ==~ /\d+\.\d+/ && version.startsWith(env.BRANCH_NAME))
}
