/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */

def call(String version) {
    def versions = (version =~ /(\d+\.\d+\.)(\d+)(-.+)?/)
    return "${versions[0][1]}${versions[0][2].toInteger() + 1}" + (versions[0][3] ?: '') + '-SNAPSHOT'
}
