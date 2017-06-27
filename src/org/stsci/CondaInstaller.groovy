package org.stsci
import org.stsci.OSInfo

class CondaInstaller implements Serializable {
    OSInfo os
    String prefix
    String dist_version
    String url
    def dist = [:]

    CondaInstaller(prefix, dist="miniconda", variant="3", version="latest") {
        def distributions = [
            miniconda: [name: 'Miniconda',
                        variant: variant,
                        baseurl: 'https://repo.continuum.io/miniconda'],
            anaconda: [name: 'Anaconda',
                       variant: variant,
                       baseurl: 'https://repo.continuum.io/archive']
        ]
        this.os = new OSInfo()
        this.dist = distributions."${dist}"
        this.dist_version = version
        this.prefix = prefix
        this.url = "${this.dist.baseurl}/" +
                  "${this.dist.name}${this.dist.variant}-" +
                  "${this.dist_version}-${this.os.name}-${this.os.arch}.sh"
    }

    void download() {

        println("Downloading $url")
        File fp = new File('installer.sh')
        def installer = fp.newOutputStream()
        installer << new URL(this.url).openStream()
        installer.close()
        println("Received ${fp.length()} bytes")
    }

    int install() {
        if (new File(this.prefix).exists()) {
            println("Skipping installation: ${this.prefix} exists.")
            return 0xFF
        }

        if (!new File('installer.sh').exists()) {
            this.download()
        }

        def cmd = "bash installer.sh -b -p ${this.prefix}"
        def proc = cmd.execute()
        def stdout = new StringBuffer()

        proc.inputStream.eachLine { println(it) }

        //proc.waitForProcessOutput(stdout, System.err)
        //print(stdout.toString())

        return proc.exitValue()
    }

    private void detect() {
    }
}

