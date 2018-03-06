terraform {
  required_version = ">= 0.10.4"
}

provider "inwx" {
  username = "${var.inwx_user}"
  password = "${var.inwx_pw}"
}

provider "hcloud" {
  token = "${var.hcloud_token}"
}

provider "null" {
  version = "~> 1.0"
}
