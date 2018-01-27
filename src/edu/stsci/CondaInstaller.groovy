package edu.stsci
import edu.stsci.OSInfo

class CondaInstaller implements Serializable {
    OSInfo os
    public Map<String, String> shell_environment
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
        this.shell_environment = [:]
	this.installer = "${this.dist.name}${this.dist.variant}-" +
                         "${this.dist_version}-${this.os.name}-${this.os.arch}.sh"
        this.url = "${this.dist.baseurl}/" + this.installer
    }

    private def runshell(String args, silent=false) {
        def cmd = new String[3]
        def proc_env = [:]

        if (this.shell_environment) {
            proc_env = this.shell_environment
        }

        cmd[0] = 'bash'
        cmd[1] = '-c'
        cmd[2] = args

        def process = new ProcessBuilder(cmd)
        process.redirectErrorStream(true)
        Map<String, String> env_tmp = process.environment()

        if (proc_env) {
            env_tmp <<= proc_env
        }

        Process p = process.start()
        if (!silent) {
            p.inputStream.eachLine { println it}
        }
        p.waitFor()
        return p
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

