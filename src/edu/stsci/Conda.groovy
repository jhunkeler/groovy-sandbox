package edu.stsci

class Conda implements Serializable {
    private String ident
    public String prefix
    public boolean prefix_exists
    public Map<String, String> shell_environment
    public String environment_name
    public ArrayList channels
    public boolean override

    Conda (prefix) {
        this.ident = '[CONDA]'
        this.prefix = prefix
        this.prefix_exists = new File(this.prefix).exists()
        this.shell_environment = [:]
        this.environment_name = ""
        this.channels = []
        this.override = false

        if (this.prefix_exists) {
            this.activate("root")
        }
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

    void activate(String conda_env) {
        def records = [:]
        def env_dump = this.runshell("source ${prefix}/bin/activate ${conda_env} ; printenv", true)

        /* Split each environment keypair by first occurance of '=' */
        env_dump.inputStream.eachLine { line ->
            if(line) {
                def pair = line.split('=', 2).collect { it.trim() }
                /* Strip out envmodules if they exist, i.e. multiline variables*/
                if (pair[0].contains("_FUNC_") || !line.contains("=")) {
                    return
                }
                records."${pair[0]}" = pair[1]
            }
        }

        /* runshell can now natively resolve this conda installation */
        this.shell_environment = records

        /* record latest conda environment name */
        this.environment_name = conda_env
    }

    def version() {
        return this.runshell("conda --version")
    }

    def verify() {
        this.runshell("echo \$PATH")
        this.runshell("which python")
        this.runshell("python -c 'from pprint import pprint; import sys; pprint(sys.path)'")
    }

    def command(String task, String... args) {
        /* Not every Conda task accepts --quiet or --yes
           so only apply $extra_args when necessary */
        ArrayList silent_args = ["--quiet", "--yes"]
        boolean use_channels = false
        boolean prompt_avoid = false
        ArrayList channels = this.channels

        switch (task) {
            case 'install':
                use_channels = true
                prompt_avoid = true
                break
            case 'create':
                use_channels = true
                prompt_avoid = true
                break
            case 'search':
                use_channels = true
            case 'env remove':
                prompt_avoid = true
                break
            default:
                break
        }

        String cmd = "${prefix}/bin/conda ${task}"
        cmd += ' ' + args.join(' ')

        if (prompt_avoid) {
            cmd += ' ' + silent_args.join(' ')
        }

        if (use_channels) {
            if (this.override) {
                cmd += " --override-channels"
                channels.add("defaults")
            }
            for (channel in channels) {
                cmd += ' -c ' + channel
            }
        }

        println("${this.ident} ${cmd}")
        return this.runshell(cmd)
    }

    boolean provides(String env_name) {
        def result = ""
        def output = this.runshell("${this.prefix}/bin/conda env list", true)

        output.inputStream.eachLine { line ->
            if (line && !line.matches('^#.*$')) {
                def tmp = line.split(' ')[0].trim()
                if (tmp == env_name) {
                    result = tmp
                    return
                }
            }
        }
        if (env_name == result) {
            return true
        }
        return false
    }

    int create(String name, String packages) {
        String args = "-n \"${name}\""
        def proc = this.command("create", args, packages)
        return proc.exitcode
    }

    int install(String packages, boolean reinstall=false) {
        String args = ""
        if (reinstall) {
            args = "--force"
        }
        def proc = this.command("install", args, packages)
        return proc.exitcode
    }

    int remove(String packages, boolean force=false) {
        String args = ""
        if (force) {
            args = "--force"
        }
        def proc = this.command("remove", args, packages)
        return proc.exitcode
    }

    int destroy(String name) {
        String args = "-n \"${name}\""
        def proc = this.command("env remove", args)
        return proc.exitcode
    }
}

