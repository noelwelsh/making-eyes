#!/bin/bash
rvm use ruby-1.9.2
racket -e '(begin (require "toc.rkt") (make-toc toc))'
jekyll --server --auto
