package org.stsci

class OSInfo implements Serializable {
    public String name
    public String version
    public String arch

    OSInfo () {
        name = 'uname -s'.execute().text.trim()
        if (name == 'Darwin') { name = 'MacOSX' }
        arch = 'uname -p'.execute().text.trim()
        if (arch.matches('^i.*86$')) { arch = 'x86' }

        this.name = name
        this.arch = arch
        this.version = 'uname -r'.execute().text.trim()
    }

}
