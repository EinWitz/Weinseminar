resource "null_resource" "provision-jupyter" {
  triggers = {
    install_jupyter_id = "${hcloud_server.jupyter.id}"
  }

  connection {
    type    = "ssh"
    host    = "${hcloud_server.jupyter.ipv4}"
    user    = "root"
    timeout = "60m"
  }

  provisioner "file" {
    content = "deb http://nginx.org/packages/debian/ stretch nginx\ndeb-src http://nginx.org/packages/debian/ stretch nginx"
    destination = "/etc/apt/sources.list.d/nginx.list"
  }

  provisioner "file" {
    source = "data/jupyter-notebook.service"
    destination = "/usr/lib/systemd/system/jupyter-notebook.service"
  }

  provisioner "file" {
    source = "data/getssl/"
    destination = "/root/.getssl"
  }

  provisioner "remote-exec" {
    inline = [
      "curl -O http://nginx.org/keys/nginx_signing.key",
      "apt-key add nginx_signing.key",
      "apt-get update",
      "apt-get upgrade -y",
      "apt-get install -y nginx",
      "adduser --gecos \"\" --disabled-login anaconda"
      "curl -O /home/anaconda/anaconda3.sh https://repo.continuum.io/archive/Anaconda3-5.1.0-Linux-x86_64.sh",
      "chmod +x /home/anaconda/anaconda3.sh && chown anaconda:anaconda /home/anaconda/anaconda3.sh",
      "sudo -u anaconda -H bash /home/anaconda/anaconda3.sh -b -p /home/anaconda/anaconda3 -f",
      "systemctl enable jupyter-notebook.service",
      "systemctl start jupyter-notebook.server",
      "curl --silent https://raw.githubusercontent.com/srvrco/getssl/master/getssl > getssl",
      "mv getssl /sbin/getssl && chmod +x /sbin/getssl"
    ]
  }

  provisioner "file" {
    source = "data/nginx/http.conf"
    destination = "/etc/nginx/conf.d/http.conf"
  }

  provisioner "file" {
    source = "data/nginx/.htpasswd"
    destination = "/etc/nginx/.htpasswd"
  }

  provisioner "remote-exec" {
    inline = [
      "service nginx restart"
      "/sbin/getssl -a -q"
    ]
  }

  provisioner "file" {
    source = "data/nginx/https.conf"
    destination = "/etc/nginx/conf.d/https.conf"
  }

  provisioner "remote-exec" {
    inline = [
      "service nginx restart"
    ]
  }
}
