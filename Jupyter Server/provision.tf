resource "null_resource" "provision-jupyter" {
  depends_on = ["hcloud_server.jupyter"]

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
    destination = "/lib/systemd/system/jupyter-notebook.service"
  }

  provisioner "remote-exec" {
    inline = [
      "wget http://nginx.org/keys/nginx_signing.key",
      "apt-key add nginx_signing.key",
      "apt-get update",
      "apt-get upgrade -y",
      "apt-get install -y nginx curl",
      "adduser --gecos \"\" --disabled-login anaconda",
      "wget https://repo.continuum.io/archive/Anaconda3-5.1.0-Linux-x86_64.sh -O /home/anaconda/anaconda3.sh",
      "chmod +x /home/anaconda/anaconda3.sh && chown anaconda:anaconda /home/anaconda/anaconda3.sh",
      "sudo -u anaconda -H bash /home/anaconda/anaconda3.sh -b -p /home/anaconda/.anaconda3 -f",
      "rm /home/anaconda/anaconda3.sh",
      "mkdir -p /home/anaconda/.anaconda3/.jupyter/",
      "echo \"export PATH=/home/anaconda/.anaconda3/bin:$PATH\" >> /home/anaconda/.bashrc"
    ]
  }

  provisioner "file" {
    source = "data/jupyter_notebook_config.json"
    destination = "/home/anaconda/.anaconda3/.jupyter/jupyter_notebook_config.json"
  }

  provisioner "remote-exec" {
    inline = [
      "systemctl enable jupyter-notebook.service",
      "systemctl start jupyter-notebook.service",
      "wget https://raw.githubusercontent.com/srvrco/getssl/master/getssl -O /sbin/getssl",
      "chmod +x /sbin/getssl",
      "mkdir -p /root/.getssl"
    ]
  }

  provisioner "file" {
    source = "data/getssl/"
    destination = "/root/.getssl"
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
      "rm /etc/nginx/conf.d/default.conf",
      "service nginx restart",
      "/sbin/getssl -a -q",
    ]
  }

  provisioner "file" {
    source = "data/nginx/https.conf"
    destination = "/etc/nginx/conf.d/https.conf"
  }

  provisioner "remote-exec" {
    inline = [
      "service nginx restart",
    ]
  }
}
