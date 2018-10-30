# Step to install Kamailio IMS to Ubuntu server 14.04

## Step 1: Set static IP

Config interface
```bash 
nano /etc/netwok/interfaces
```
Content
```
auto eth0
iface eth0 inet static
        address 192.168.122.39
        netmask 255.255.255.0
        gateway 192.168.122.1
        dns-nameservers 192.168.122.252 192.168.122.39
```
Restart network  
```
/etc/init.d/network restart
```

## Step 2: Set up DNSZone

Install bind9
```
apt-get install bind9
```
Config `name.conf.local` file
```
nano /etc/bind/name.conf.local
```
Add at the end of file
```
zone "open-ims.test"  {
        type master;
        file "/etc/bind/sixteen-dek.org.dnszone";
};
```
Create `sixteen-dek.org.dnszone` file
```
nano /etc/bind/sixteen-dek.org.dnszone
```
Add
```
$ORIGIN open-ims.test.
$TTL 1W
@                       1D IN SOA       localhost. root.localhost. (
                                        2006101001      ; serial
                                        3H              ; refresh
                                        15M             ; retry
                                        1W              ; expiry
                                        1D )            ; minimum

                        1D IN NS        ns
ns                      1D IN A         192.168.122.39

pcscf                   1D IN A         192.168.122.39
_sip.pcscf              1D SRV 0 0 5060 pcscf
_sip._udp.pcscf         1D SRV 0 0 5060 pcscf
_sip._tcp.pcscf         1D SRV 0 0 5060 pcscf


icscf                   1D IN A         192.168.122.39
_sip                    1D SRV 0 0 4060 icscf
_sip._udp               1D SRV 0 0 4060 icscf
_sip._tcp               1D SRV 0 0 4060 icscf

open-ims.test.          1D IN A         192.168.122.39
open-ims.test.          1D IN NAPTR 10 50 "s" "SIP+D2U" ""      _sip._udp
open-ims.test.          1D IN NAPTR 20 50 "s" "SIP+D2T" ""      _sip._tcp


scscf                   1D IN A         192.168.122.39
_sip.scscf              1D SRV 0 0 6060 scscf
_sip._udp.scscf         1D SRV 0 0 6060 scscf
_sip._tcp.scscf         1D SRV 0 0 6060 scscf


trcf                    1D IN A         192.168.122.39
_sip.trcf               1D SRV 0 0 3060 trcf
_sip._udp.trcf          1D SRV 0 0 3060 trcf
_sip._tcp.trcf          1D SRV 0 0 3060 trcf


bgcf                    1D IN A         192.168.122.39
_sip.bgcf               1D SRV 0 0 7060 bgcf
_sip._udp.bgcf          1D SRV 0 0 7060 bgcf
_sip._tcp.bgcf          1D SRV 0 0 7060 bgcf


mgcf                    1D IN A         192.168.122.39
_sip.mgcf               1D SRV 0 0 8060 mgcf
_sip._udp.mgcf          1D SRV 0 0 8060 mgcf
_sip._tcp.mgcf          1D SRV 0 0 8060 mgcf


hss                     1D IN A         192.168.122.39

ue                      1D IN A         192.168.122.39

presence                1D IN A         192.168.122.39

pcrf                                    1D IN A                 192.168.122.39
clf                                     1D IN A                 192.168.122.39
```
Reset bind9
```
/etc/init.d/bind9 restart
```
Config `resolv.conf` file
```
nano /etc/resolv.conf
```
Content of file 
```
nameserver 192.168.122.39
search open-ims.test
domain open-ims.test
```
#### Test
```
dig pcscf.open-ims.test
ping pcscsf.open-ims.test
```

## Step 3: Install Kamailio IMS
Get install file
```
wget http://repository.ng-voice.com/install.sh
```
Change `chmod` foe `install.sh`
```
chmod 777 ./install.sh
```
Run `install.sh`
```
./install.sh
```
Install Mysql Server 5.5
```
apt-get install mysql-server
```
### Install PCSCF  
```
apt-get ims-pcscf
```
Configurate `ims-pcscf`
Interface for Receive SIP-Requests:
- Both  

What IP should be advertised in the headers of SIP-Requests:
- null

What is the hostname for this host?
- pcscf.open-ims.test

Please select the supported network protocols on this host ?
- UDP
- TCP

Forward requests to an SBC
- none

Active RAVEL
- null

Block failed requests?
- none

Disable active SIP-Trace intergrration
- null

Enable NAT-Ping?
- UDP

SUBSCRIBE/PUBLISH
- Yes

XML-RPC
- Yes

Allow XML from 
- 127.0.0.1

Rx-Interface
- No

Primary Database
- 127.0.0.1

Secondary Database
- null

Database name
- pcscf

Create MySQL database
- Yes

Storage engine
- default

Translate TEL-URI to SIP-URI
- Yes

### Install ICSCF
```
apt-get ims-icscf
```
Configurate `ims-icscf`
Interface for Receive SIP-Requests:
- Both  

What IP should be advertised in the headers of SIP-Requests:
- null

What is the hostname for this host?
- icscf.open-ims.test

Please select the supported network protocols on this host ?
- UDP

Port 
- 4060

XML-RPC
- Yes

Allow XML from 
- 127.0.0.1

Automate Cx/Dx
- Yes

Port for Cx/Dx
- 3869

HSS address
- hss.open-ims.test

Primary Database
- 127.0.0.1

Secondary Database
- null

Create MySQL database
- Yes

Storage engine
- default

### Install SCSCF
```
apt-get ims-scscf
```
Configurate `ims-icscf`
Interface for Receive SIP-Requests:
- Both  

What IP should be advertised in the headers of SIP-Requests:
- null

What is the hostname for this host?
- scscf.open-ims.test

Please select the supported network protocols on this host ?
- UDP

Port 
- 6060

XML-RPC
- Yes

Online Charging
- No

Authentication SIM
- No

Allow XML from 
- 127.0.0.1

Automate Cx/Dx
- Yes

Port for Cx/Dx
- 3870

HSS address
- hss.open-ims.test

Primary Database
- 127.0.0.1

Secondary Database
- null

Create MySQL database
- Yes

Storage engine
- default

### Install java
``` 
apt-get isntall default-jdk
```

### Config `ferm`
```
nano /etc/ferm/ferm.conf
```
Change `policy DROP` to `policy ACCEPT`

### Install FHoSS
```
mkdir /opt/OpenIMSCore 
cd /opt/OpenIMSCore

mkdir FHoSS 
svn checkout https://svn.code.sf.net/p/openimscore/code/FHoSS/trunk FHoSS


cd FHoSS 
ant compile deploy 
cd ..

mysql -u root -p < FHoSS/scripts/hss_db.sql 
mysql -u root -p < FHoSS/scripts/userdata.sql
```

Check Diameter setting
```
cd /opt/OpenIMSCore/FHoSS/deploy
nano DiameterPeerHSS.xsml
```
Add JAVA_HOME to config
```
nano ./startup.sh
```
Add on top
```
JAVAHOME="/usr/lib/jvm/default-java/"
```
Run FHoSS (Should use screen to run in background)
```
./startup.sh
```

### Config PCSCF
Change dir to pcscf
```
cd /etc/kamailio_pcscf
```
Config `pcscf.cfg`
- Change `listen` to correct IP and port
- Change `alias` to correct one
Config `kamailio.cfg`
- Disable all `dispatcher`
Restart PCSCF
- /etc/init.d/kamailio_pcscf restart
Result must be the same as below
```
 * Stopping Kamailio SIP Server: kamailio_pcscf                                                                                                                  [ OK ]
 * Starting Kamailio SIP Server: kamailio_pcscf                                                                                                                         loading modules under config path: /usr/lib64/kamailio/modules_k/:/usr/lib64/kamailio/modules/:/usr/local/lib/kamailio/modules/
Listening on
             udp: 192.168.122.39 [192.168.122.39]:5060
             udp: 127.0.0.1:5060
             tcp: 192.168.122.39 [192.168.122.39]:5060
             tcp: 127.0.0.1:5060
Aliases:
             *: pcscf.open-ims.test:*
```

### Config ICSCF
Change dir to icscf
```
cd /etc/kamailio_icscf
```
Config `icscf.cfg`
- Change `listen` to correct IP and port
- Change `alias` to correct one (ims.open-ims.test)
Config `icscf.xml`
- Change `Realm` into correct url
- Check correct port
Restart ICSCF
- /etc/init.d/kamailio_icscf restart
Result must be the same as below
```
 * Stopping Kamailio SIP Server: kamailio_icscf                                                                                                                  [ OK ]
 * Starting Kamailio SIP Server: kamailio_icscf                                                                                                                         loading modules under config path: /usr/lib64/kamailio/modules_k/:/usr/lib64/kamailio/modules/:/usr/lib/kamailio/modules_k/:/usr/lib/kamailio/modules/
Listening on
             udp: 192.168.122.39 [192.168.122.39]:4060
             udp: 127.0.0.1:4060
             tcp: 127.0.0.1:4060
Aliases:
             *: ims.open-ims.test:*
```

Config SCSCF
Change dir to scscf
```
cd /etc/kamailio_scscf
```
Config `scscf.cfg`
- Change `listen` to correct IP and port
- Change `alias` to correct one (ims.open-ims.test)
Config `scscf.xml`
- Change `Realm` into correct url
- Check correct port
Restart ICSCF
- /etc/init.d/kamailio_scscf restart
Result must be the same as below
```
 * Stopping Kamailio SIP Server: kamailio_scscf                                                                                                                  [ OK ]
 * Starting Kamailio SIP Server: kamailio_scscf                                                                                                                         loading modules under config path: /usr/lib64/kamailio/modules_k/:/usr/lib64/kamailio/modules/:/usr/lib/kamailio/modules_k/:/usr/lib/kamailio/modules/
Listening on
             udp: 192.168.122.39 [192.168.122.39]:6060
             udp: 127.0.0.1:6060
             tcp: 127.0.0.1:6060
Aliases:
             *: scscf.open-ims.test:*
             *: open-ims.test:*
             *: ims.open-ims.test:*
```

### Check Port
```
netstat -plnt
```
Result
```
Active Internet connections (only servers)
Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name
tcp        0      0 192.168.122.39:53       0.0.0.0:*               LISTEN      30774/named
tcp        0      0 127.0.0.1:53            0.0.0.0:*               LISTEN      30774/named
tcp        0      0 0.0.0.0:22              0.0.0.0:*               LISTEN      1090/sshd
tcp        0      0 127.0.0.1:953           0.0.0.0:*               LISTEN      30774/named
tcp        0      0 127.0.0.1:4060          0.0.0.0:*               LISTEN      733/kamailio
tcp        0      0 192.168.122.39:3869     0.0.0.0:*               LISTEN      728/kamailio
tcp        0      0 192.168.122.39:3870     0.0.0.0:*               LISTEN      843/kamailio
tcp        0      0 127.0.0.1:5060          0.0.0.0:*               LISTEN      442/kamailio
tcp        0      0 192.168.122.39:5060     0.0.0.0:*               LISTEN      442/kamailio
tcp        0      0 127.0.0.1:3306          0.0.0.0:*               LISTEN      16460/mysqld
tcp        0      0 127.0.0.1:6379          0.0.0.0:*               LISTEN      11365/redis-server
tcp        0      0 127.0.0.1:6060          0.0.0.0:*               LISTEN      853/kamailio
tcp        0      0 0.0.0.0:2000            0.0.0.0:*               LISTEN      540/asterisk
tcp6       0      0 :::53                   :::*                    LISTEN      30774/named
tcp6       0      0 ::1:953                 :::*                    LISTEN      30774/named
tcp6       0      0 127.0.0.1:3868          :::*                    LISTEN      31843/java
tcp6       0      0 127.0.0.1:8080          :::*                    LISTEN      31843/java
tcp6       0      0 :::80                   :::*                    LISTEN      5331/apache2
```