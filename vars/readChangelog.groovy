/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */

def call() {
    def lines = readFile("CHANGELOG.md").split("\n")
    for (i = 0; i < lines.length; i++) {
        def m = lines[i] =~ /##\s+\[(\d+\.\d+\.\d+.*)]\s+-\s+(\d+-\d+-\d+)/
        if (m) {
            return [version:m.group(1), date:m.group(2)]
        }
    }
    error("No semver version defined in the CHANGELOG.md")
}
