resource "inwx_record" "dns" {
  domain = "strtpvlla.net"
  type = "A"
  name = "jupyter"
  value = "${hcloud_server.jupyter.ipv4}"
}
