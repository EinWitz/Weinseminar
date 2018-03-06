variable "inwx_user" {
  type = "string"
  description = "INWX Username"
}

variable "inwx_pw" {
  type = "string"
  description = "INWX Password"
}

variable "hcloud_token" {
  type = "string"
  description = "Hetzner Cloud Access Token"
}

variable "server_type" {
  type = "string"
  description = "Hetzner Cloud Server Type for Jupyter Server"
}

variable "ssh_authorized_key" {
  type        = "string"
  description = "SSH public key to set as an authorized_key on machines"
}
