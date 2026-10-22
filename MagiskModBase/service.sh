PKGNAME="sh.anirudhgupta109.pxn"
LEGACY_PKGNAME="sh.siava.pixelxpert"
LSPDDBPATH="/data/adb/lspd/config/modules_config.db" 
MAGISKDBPATH="/data/adb/magisk.db" 
MODDIR=${0%/*} 
 
prepareSQL(){ 
	chmod +x $MODDIR/sqlite3
	SQLITEPATH="$MODDIR/sqlite3" 
} 
 
# runSQL "database path" "command" - then you can use $SQLRESULT to read the outcome 
runSQL(){ 
	SQLRESULT=$($SQLITEPATH $DBPATH "$CMD") 
} 
 
#grant silent root access to given UID 
grantRootUID(){ 
	if [ -z "$1" ]; then
		return 1
	fi
	DBPATH=$MAGISKDBPATH 
	 
	#new record - older magisk compatibility 
	CMD="insert into policies (uid, package_name, policy, until, logging, notification) values ($1, '$2', 2, 0, 1, 0);" && runSQL 
	#new record 
	CMD="insert into policies (uid, policy, until, logging, notification) values ($1, 2, 0, 1, 0);" && runSQL 
	#previously present record 
	CMD="update policies set policy = 2, until = 0, logging = 1, notification = 0 where uid = $1;" && runSQL 
} 
 
 
#grant root access to given package name 
grantRootPkg(){ 
	echo "- 	Granting root access to $1..." 
	UID=$(pm list packages -U $1 --user 0 | grep ":$1 " | awk -F 'uid:' '{ print $2 }' | cut -d ',' -f 1)
 
	grantRootUID $UID $1 
} 
 
#grant root access to required apps 
grantRootApps(){ 
	grantRootPkg $PKGNAME
}

prepareSQL 
 
if [ -n "$MAGISK_VER_CODE" ] && [ "$KSU" != "true" ] && [ "$APATCH" != "true" ]; then
	grantRootApps
fi

# Wait for boot to finish
until [ "$(getprop sys.boot_completed)" = "1" ]; do
	sleep 1
done

if pm list packages | grep -q "package:$LEGACY_PKGNAME"; then
	echo "- Removing legacy $LEGACY_PKGNAME..."
	pm uninstall --user 0 "$LEGACY_PKGNAME" > /dev/null 2>&1
fi

if [ -f "$MODDIR/install_needed" ]; then
	pm install -r -d "$MODDIR/PixelXpert-Next.apk" > /dev/null 2>&1
	rm -f "$MODDIR/install_needed"
fi
