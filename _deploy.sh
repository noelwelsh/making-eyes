#!/bin/bash
rvm use ruby-1.9.2
racket -e '(begin (require "toc.rkt") (make-toc toc))'
cp _config-deploy.yml _config.yml
jekyll
rsync -av _site/* admin@unweb:/srv/noelwelsh.com/public/htdocs/blueeyes
cp _config-view.yml _config.yml
