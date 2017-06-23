import java.lang.*

class Conda {
    public String prefix
    public boolean prefix_exists
    public Map<String, String> sh_environment
    public String conda_environment

    Conda (prefix) {
        this.prefix = prefix
        this.prefix_exists = new File(this.prefix).exists()
        this.sh_environment = [:]
        this.conda_environment = ""

        /*if (this.prefix_exists) {
            println('Activating root environment')
            this.sh_environment = this.activate("root")
        }*/
    }

    private def runshell(String args, silent=false) {
        def cmd = new String[3]
        def proc_env = [:]

        if (this.sh_environment) {
            proc_env = this.sh_environment
        }

        cmd[0] = 'bash'
        cmd[1] = '-c'
        cmd[2] = args

        def process = new ProcessBuilder(cmd)
        process.redirectErrorStream(true)
        Map<String, String> env_tmp = process.environment()
        env_tmp <<= proc_env

        Process p = process.start()
        if (!silent) {
            p.inputStream.eachLine { println it}
        }
        p.waitFor()
        return p
    }

    void activate(String conda_env) {
        def records = [:]
        def bah = this.runshell("source ${prefix}/bin/activate ${conda_env} ; printenv", true)
        bah.inputStream.eachLine { line ->
            if(line) {
                def (k, v) = line.split('=', 2).collect { it.trim() }
                records."$k" = v
            }
        }
        this.sh_environment = records
        this.conda_environment = conda_env
    }

    def version() {
        return this.runshell("conda --version")
    }

    def verify() {
        this.runshell("echo \$PATH")
        this.runshell("which python")
        this.runshell("python -c 'from pprint import pprint; import sys; pprint(sys.path)'")
    }

    void create(String name, String packages) {
        println("Creating environment: ${name}")
        this.runshell("conda create --yes -n \"${name}\" ${packages}")
    }

    void install() {
    }

}

static void main(String[] args) {
    println("OK")
    c = new Conda("/users/jhunk/anaconda3")
    c.activate("root")
    println("Conda exists: ${c.prefix_exists}")
    c.verify()
    c.create("groovy", "python=3.5")
    c.activate("groovy")
    c.verify()
}


