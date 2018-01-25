import edu.stsci.*

static void main(String[] args) {
    final String PREFIX = "/tmp/miniconda3"
    final String NAME = "astroconda35"
    final String PKGS = "python=3.5 numpy=1.12 drizzlepac"

    cinst = new CondaInstaller(PREFIX)
    cinst.install()

    c = new Conda(PREFIX)
    assert c.prefix_exists == true

    c.override = true
    c.channels.add("astropy")
    c.channels.add("http://ssb.stsci.edu/astroconda")
    c.channels.add("conda-forge")

    if (c.provides(NAME)) {
        assert c.destroy(NAME) == 0
    }
    assert c.create(NAME, PKGS) == 0
    c.activate(NAME)
    assert c.environment_name == NAME
    assert c.provides(NAME) == true
}

