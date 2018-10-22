FROM httpd:alpine

COPY ./build/* /usr/local/apache2/htdocs/
