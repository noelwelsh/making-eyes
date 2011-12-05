#!/bin/bash
rvm use ruby-1.9.2
jekyll
rsync -av _site/* admin@unweb:/srv/noelwelsh.com/public/htdocs/blueeyes