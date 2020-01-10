quit
exit
ls
pwd
tar -xzf apache-tomcat-9.0.16.tar.gz 
ls
cd apache-tomcat-9.0.16/
ls
exit
ls
cd apache-tomcat-9.0.16/
ls
cd webapps/
ls
cp ~/intrx_services.war .
ls
cd ..
ls
cd conf
ls
vi context.xml 
vi server.xml 
vi web.xml 
cd ..
cd webapps/
ls
unzip intrx_services.war 
ls
ls -ltr
mv WEB-INF/ ROOT/.
cd ROOT
ls
rm -rf *
cd ..
mv WEB-INF/ ROOT/.
mv verify_email_template.html ROOT/.
mv favicon.ico ROOT/.
ls -ltr
mv assets/ ROOT/.
ls
ls -ltr
mv META-INF/ ROOT/.
mv web ROOT/.
ls -ltr
rm intrx_services.war 
ls
cd ..
ls
cd bin
ls
./startup.sh 
java
sudo yum list java -1\*
sudo yum list \*java-1\*
sudo yum install java-1.8.0-openjdk-devel.x86_64
java
./startup.sh 
tail -f ../logs/catalina.out
netstat -a | grep 80
ls
cd ../logs/
ls -ltr
vi localhost.2019-02-28.log
vi catalina.out
cd ../bin
sudo ./startup.sh 
tail -f ../logs/catalina.out
cd ../logs
ls
ls -ltr
vi catalina.out
netstat -a | grep 80
cd ../bin
ls
vi catalina.sh
rm ../logs/catalina.out
sudo ./startup.sh 
tail -f ../logs/catalina.out
cd ../logs
ls
ls -ltr
vi catalina.out
sudo vi catalina.out
cd ..
cd conf/
ls
ls -ltr
vi server.xml 
cd ../bin
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
cd ../logs
ls -ltr
sudo vi catalina.out
sudo lsof -i:8005
cd ../bin
sudo ./shutdown.sh
./shutdown.sh 
sudo rm ../logs/catalina.out
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
sudo ./shutdown.sh 
sudo vi ../conf/server.xml 
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
sudo vi ../logs/catalina.out
sudo tail -f ../logs/catalina.out
exit
ls
cd apache-tomcat-9.0.16/
ls
cd webapps/
ls
cd ..
ls
unzip intrx_services.war 
ls
cd apache-tomcat-9.0.16/webapps/ROOT/
ls
cd WEB-INF/
ls
cd classes/
ls
cd ..
ls
mv ~/assets .
rm -rf *
mv ~/assets .
mv ~/favicon.ico .
mv ~/META-INF .
mv ~/verify_email_template.html .
mv ~/web .
mv ~/WEB-INF .
ls -ltr
cd ..
ls
cd apache-tomcat-9.0.16/bin/
ls
ps -ef | grep tomcat
./shutdown.sh
sudo ./shutdown.sh 
sudo ./startup.sh 
ls
exit
ls
mv s.war apache-tomcat-9.0.16/webapps/.
rm intrx_services.war 
ls
mkdir temp
ls
mv intrx_web.tar temp/.
cd temp
ls
tar -xvf intrx_web.tar 
ls
cd ../../ec2-user/apache-tomcat-9.0.16/webapps/ROOT/
ls
rm -rf *
cp -rf ~/temp/dist/ .
ls
cd dist
ls
mv * ../.
ls
cd ..
ls
rm -rf dist
cd ..
cd ../bin
ls
sudo ./shutdown.sh
sudo ./startup.sh 
cd ..
cd webapps/
ls
cd s/
ls
cd s
ls -ltr
sudo cd s
ls -ltr
pwd
sudo cd s
cd s
cd ../bin/
sudo ./shutdown.sh 
cd ../webapps/
ls
ls -ltr
rm -rf s
sudo rm -rf s
ls -ltr
mkdir temp
mv s.war temp/.
ls
cd temp
ls
unzip s.war
ls -ltr
ls
cd ..
ls
mv temp s
ls 
cd ../bin
sudo ./startup.sh 
cd ..
cd bin
sudo ./shutdown.sh
cd ..
ls
cd temp
ls
rm -rf *
ls
wget --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/9.0.1+11/jdk-9.0.1_linux-x64_bin.rpm
wget --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/11.0.2+9/f51449fcd52f4d52b93a989c5c56ed3c/jdk-11.0.2_linux-x64_bin.rpm
ls
java
java -version
sudo yum install jdk-11.0.2_linux-x64_bin.rpm 
java -version
cd ..
cd apache-tomcat-9.0.16/bin
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
sudo vi ../logs/catalina.out
vi ../conf/server.xml 
sudo ./shutdown.sh 
cd apache-tomcat-9.0.16/
cd webapps/
ls
rm -rf ROOT
mv s ROOT
ls
cd ..
ls
cd ..
ls
cd temp
ls
cd ..
sudo wget http://nginx.org/keys/nginx_signing.key
sudo apt-key add nginx_signing.key
ls
amazon-linux-extras list
sudo amazon-linux-extras install nginx1.12
exit
ls
tar -xvf intrx_web.tar 
ls -ltr
mv dist web
ls
cd /etc/nginx/
ls
vi nginx.conf
pwd
cd -
cd /etc/nginx/
vi nginx.conf
sudo vi nginx.conf
sudo systemctl restart nginx
sudo vi nginx.conf
sudo systemctl restart nginx
ps -ef | grep nginx
cd ~
sudo chown -R nginx:nginx web
sudo chmod 755 web
cd -
sudo systemctl restart nginx
vi nginx.conf
cd ~
sudo chown -R root:nginx web
sudo chmod -R 775 web
cd -
sudo systemctl restart nginx
sudo chkconfig nginx on
sudo chown -R nginx:nginx ~/web/*
sudo chmod -R 0755 ~/web/*
sudo systemctl restart nginx
sudo chmod -4 +x ~/web
vi nginx.conf
sudo vi nginx.conf
sudo systemctl restart nginx
sudo vi nginx.conf
sudo systemctl stop nginx
ls
cd web
ls
ls -ltr
cd ..
ls -ltr
history | grep chown
sudo chown -R ec2-user:ec2-user web/*
ls
sudo chown -R ec2-user:ec2-user ~/web/*
ls -ltr
sudo rm -rf web/
tar -xvf intrx_web.tar 
ls
npm install http-server -g
yum install npm
sudo yum install node
sudo yum install npm
sudo yum install nodejs npm --enablerepo=epel
sudo yum install nodejs npm
sudo yum update
http://www.wisdomofjim.com/blog/how-to-install-node-and-npm-on-an-ec2-server
sudo yum install gcc-c++ make
sudo yum install openssl-devel
sudo yum install git
git clone git://github.com/joyent/node.git
ls
cd node
git checkout v0.6.8
./configure
make
ls
rm -rf *
git tag -l
cd ..
ls
rm -rf node
git clone https://github.com/nodejs/node.git
cd node
git tag -l
git checkout v11.10.1
./configure
make
ls
sudo make install
sudo su
ls -ltr
cd ..
ls
ls -ltr
sudo rm -rf npm
git clone https://github.com/isaacs/npm.git
cd npm
sudo env PATH=$HOME/local/node/bin:$PATH make install
npm
npm install http-server -g
sudo npm install http-server -g
cd ..
ls
http-server ./dist
http-server ./dist -p 80
sudo http-server ./dist -p 80
ls
history | grep nginx
sudo  vi /etc/nginx/nginx.conf
sudo systemctl restart nginx
sudo  vi /etc/nginx/nginx.conf
sudo systemctl restart nginx
sudo  vi /etc/nginx/nginx.conf
sudo systemctl restart nginx
sudo  vi /etc/nginx/nginx.conf
pwd
cd apache-tomcat-9.0.16/logs/
vi catalina.out
sudo vi catalina.out
cd ../bin
sudo ./shutdown.sh 
./shutdown.sh 
ls
ls -ltr
touch setenv.sh
chmod 777 setenv.sh
vi setenv.sh 
sudo ./startup.sh 
sudo vi ../logs/catalina.out
ls -ltr
vi setenv.sh 
sudo ./shutdown.sh 
sudo ./startup.sh 
sudo vi ../logs/catalina.out
ls
find . -type f -print | xargs grep "gmail"
find . -type f -print | xargs grep "setenv.sh"
sudo vi ../logs/catalina.out
cd ..
cd webapps/
ls
cd ROOT
ls
cd WEB-INF/
ls
cd classes/
ls
vi log4j2.xml 
cd ..
cd logs/
vi catalina.out
sudo vi catalina.out
vi ../webapps/ROOT/WEB-INF/classes/log4j2.xml 
exit
ls
cd temp
ls
mv ../s.war .
ls
unzip s.war
ls -ltr
mv WEB-INF/ ../apache-tomcat-9.0.16/webapps/ROOT/.
cd ../apache-tomcat-9.0.16/webapps/ROOT
ls
rm -rf *
mv ~/temp/WEB-INF .
mv ~/temp/META-INF .
ls -ltr
cd ..
cd bin
sudo ./shutdown.sh
sudo ./startup.sh 
sudo vi ../logs/catalina.out
cd ../conf/
ls
vi catalina.properties 
vi ../bin/setenv.sh 
vi catalina.properties 
cd ..
cd bin
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
vi ../bin/setenv.sh 
vi ~/.bash_profile 
cd ~
. ./.bash_profile 
cd apache-tomcat-9.0.16/bin
ls
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
chmod +x setenv.sh 
sudo ./shutdown.sh 
sudo ./startup.sh 
tail -f ../logs/catalina.out
sudo tail -f ../logs/catalina.out
vi setenv.sh 
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
sudo ./shutdown.sh 
sudo systemctl restart nginx
sudo systemctl stop nginx
ls
cd ~
ls
cd apache-tomcat-9.0.16/webapps/
ls
mv ROOT s
mkdir ROOT
cd ROOT
cp -rf ~/dist/* .
ls -ltr
cd ../../conf/
ls
vi web.xml 
cd ../
cd bin
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
vi ../conf/context.xml 
vi ../conf/server.xml 
sudo ./shutdown.sh
sudo ./startup.sh 
ls
rm -rf dist
mkdir dist
mv intrx_web.tar dist/.
cd dist
ls
tar -xvf intrx_web.tar 
ls
rm intrx_web.tar 
cd dist
ls
mv * ../.
cd ..
ls
rm -rf dist
ls
cd ../apache-tomcat-9.0.16/webapps/ROOT/
ls
cp -rf ~/dist/* .
cd ..
cd bin
sudo ./shutdown.sh 
sudo ./startup.sh 
sudo vi apache-tomcat-9.0.16/logs/catalina.out
cd apache-tomcat-9.0.16/bin
vi setenv.sh 
sudo vi ../logs/catalina.out
vi setenv.sh 
cd ../webapps/
ls
cp -rf ~/s.war .
ls
cd ../bin
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
vi setenv.sh 
pwd
cp ~/s.war ../webapps/.
sudo ./shutdown.sh 
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
ls
cp s.war apache-tomcat-9.0.16/webapps/.
cd apache-tomcat-9.0.16/
cd bin
sudo ./shutdown.sh
sudo ./startup.sh
vi setenv.sh 
cp ~/s.war ../webapps/.
sudo ./shutdown.sh
sudo rm ../logs/catalina.out
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
cp ~/s.war ../webapps/.
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
cp ~/s.war ../webapps/.
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
ls
cd ..
find . -type f -print | xargs grep "gmail"
find . -type f -print | xargs grep "8080"
find . -type f -print | xargs grep "154:8080"
find . -type f -print | xargs grep "3.95.252.154:8080"
find . -type f -print | xargs grep "3.95.252.154:8080" > ek.txt
ls
cd bin
find . -type f -print | xargs grep "3.95.252.154:8080"
vi setenv.sh 
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
ls
cp ~/s.war ../webapps/.
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
cp ~/s.war ../webapps/.
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
cp ~/s.war ../webapps/.
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
vi setenv.sh 
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
cp ~/s.war ../webapps/.
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
exit
cp s.war apache-tomcat-9.0.16/webapps/.
cd apache-tomcat-9.0.16/bin
sudo ./shutdown.sh
sudo ./startup.sh 
cp s.war apache-tomcat-9.0.16/webapps/.
cd apache-tomcat-9.0.16/bin
sudo ./shutdown.sh
sudo ./startup.sh
exit
cp s.war apache-tomcat-9.0.16/webapps/.
cd apache-tomcat-9.0.16/bin
sudo ./shutdown.sh
sudo ./startup.sh
exit
ls
tar -xvf intrx_web.tar 
cd dist
ls
cp -rf * ../apache-tomcat-9.0.16/webapps/ROOT/.
cd ../apache-tomcat-9.0.16/bin
ls
sudo ./shutdown.sh
sudo ./startup.sh 
cp s.war  apache-tomcat-9.0.16/webapps/.
exit
ls
tar -xvf dist/
rm -rf dist
tar -xvf intrx_web.tar 
ls
cddist
cd dist
ls
mv -rf * ../apache-tomcat-9.0.16/webapps/ROOT/.
cp -rf * ../apache-tomcat-9.0.16/webapps/ROOT/.
cd ../apache-tomcat-9.0.16/bin
sudo ./shutdown.sh
sudo ./startup.sh 
pwd
exit
ls
rm -rf dist
tar -xvf intrx_web.tar 
cd dist
ls
cp -rf * ../apache-tomcat-9.0.16/webapps/ROOT/.
cd ../apache-tomcat-9.0.16/bin
sudo ./shutdown.sh
sudo ./startup.sh 
ls
exit
sp s.war apache-tomcat-9.0.16/webapps/.
cd apache-tomcat-9.0.16/bin
sudo ./shutdown.sh
sudo ./startup.sh 
sudo vi ../logs/catalina.out
sudo ./shutdown.sh 
sudo rm ../logs/catalina.out
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
exit
cp s.war apache-tomcat-9.0.16/webapps/.
cd apache-tomcat-9.0.16/bin
sudo ./shutdown.sh
sudo ./startup.sh 
sudo tail -f ../logs/catalina.out
exit
ls
rm -rf dist
tar -xvf intrx_web.tar 
ls
cd dist
ls
cp -rf * ../apache-tomcat-9.0.16/webapps/ROOT/.
ls
cd ..
cd apache-tomcat-9.0.16/bin
sudo ./shutdown.sh 
sudo ./startup.sh 
ls
ls -ltr
cp s.war apache-tomcat-9.0.16/webapps/.
cd apache-tomcat-9.0.16/bin
sudo ./shutdown.sh 
sudo ./startup.sh 
exit
cp s.war apache-tomcat-9.0.16/webapps/.
cd apache-tomcat-9.0.16/bin
sudo ./shutdown.sh 
sudo ./startup.sh 
exit
cp s.war apache-tomcat-9.0.16/webapps/.
cd apache-tomcat-9.0.16/bin
sudo ./shutdown.sh 
sudo ./startup.sh 
