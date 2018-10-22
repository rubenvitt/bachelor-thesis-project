FROM httpd:alpine

COPY ./build/* /usr/local/apache2/htdocs/
RUN mkdir /usr/local/apache2/htdocs/assets
RUN mv /usr/local/apache2/htdocs/static /usr/local/apache2/htdocs/assets/
