import edu.stsci.*

static void main(String[] args) {
    final String PREFIX = "/tmp/miniconda3"
    final String NAME = "py36"
    final String PKGS = "python=3.6 numpy astropy"

    cinst = new CondaInstaller(PREFIX)
    cinst.install()

    c = new Conda(PREFIX)
    assert c.prefix_exists == true

    c.override = true
    c.channels.add("http://ssb.stsci.edu/astroconda")

    if (c.provides(NAME)) {
        assert c.destroy(NAME) == 0
    }
    assert c.create(NAME, PKGS) == 0
    c.activate(NAME)
    assert c.environment_name == NAME
    assert c.provides(NAME) == true
    c.runshell("python --version")
}

