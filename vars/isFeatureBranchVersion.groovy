/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */

def call(String version) {
    return version ==~ /\d+\.\d+\.\d+(:?-.+)?-\p{Upper}+-\d+(:?-SNAPSHOT)?/
}
