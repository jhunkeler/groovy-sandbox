package edu.stsci
import edu.stsci.OSInfo

class CondaInstaller implements Serializable {
    OSInfo os
    private String ident
    String prefix
    String dist_version
    String installer
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

	this.ident = '[CONDA INSTALLER]'
        this.os = new OSInfo()
        this.dist = distributions."${dist}"
        this.dist_version = version
        this.prefix = prefix
	this.installer = "${this.dist.name}${this.dist.variant}-" +
                         "${this.dist_version}-${this.os.name}-${this.os.arch}.sh"
        this.url = "${this.dist.baseurl}/" + this.installer
    }

    int download() {
        println("${this.ident} Downloading $url")
        def proc = ["bash", "-c", "curl -sLO ${this.url}"].execute()
        def out = new StringBuffer()
        def err = new StringBuffer()
        proc.consumeProcessOutput(out, err)
        proc.waitFor()

        if (out.size() > 0) println(out)
        if (err.size() > 0) println(err)
        // Whatever Jenkins... Why is this so hard?
        /*
        File fp = new File(this.installer)
        def body = fp.newOutputStream()
        body << new URL(this.url).openStream()
        body.close()
        println("${this.ident} Received ${fp.length()} bytes")
        */
        return proc.exitValue()
    }

    int install() {
        if (new File(this.prefix).exists()) {
            println("${this.ident} ${this.prefix} exists.")
            return 0xFF
        }

        //if (!new File(this.installer).exists()) {
            this.download()
        //}

        def cmd = "bash ${this.installer} -b -p ${this.prefix}"
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

