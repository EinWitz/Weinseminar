data "hcloud_servertype" "jupyter" {
  name = "${var.server_type}"
}

resource "hcloud_sshkey" "jupyter-key" {
    name = "Jupyter KeyEvent"
    public_key = "${var.ssh_authorized_key}"
}

data "hcloud_server" "jupyter" {
  name = "jupyter.strtpvlla.net"
  server_type = "${data.hcloud_servertype.jupyter.id}"
  image = "2"
  ipv4_ptr = "jupyter.strtpvlla.net"

  ssh_keys = [
        "${hcloud_sshkey.jupyter.id}"
    ]
}
