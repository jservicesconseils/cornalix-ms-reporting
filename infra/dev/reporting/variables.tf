variable "project" {
  type    = string
  default = "cornalix"
}

variable "environment" {
  type    = string
  default = "dev"
}

variable "aws_region" {
  type    = string
  default = "ca-central-1"
}

variable "service_name" {
  type    = string
  default = "reporting"
}

variable "container_port" {
  type    = number
  default = 8084
}

variable "image_tag" {
  type    = string
  default = "dev"
}

variable "public_hostname" {
  type    = string
  default = "dev-reporting.api.cornalix.ca"
}

variable "diagnostic_hostname" {
  type    = string
  default = "dev-diagnostic.api.cornalix.ca"
}

variable "scoring_hostname" {
  type    = string
  default = "dev-scoring.api.cornalix.ca"
}

variable "alb_rule_priority" {
  type    = number
  default = 204
}
