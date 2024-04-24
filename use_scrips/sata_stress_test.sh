#! /system/bin/sh

#for andriod, default mount and output path
OUT_PATH=/mnt/sata

#single file size, BLOCK_SIZE * BLOCK_COUNT (Byte)
BLOCK_SIZE=512
BLOCK_COUNT=102400
#file count, auto calculation
FILE_COUNT=0
#emmc avail size, auto claculation
AVAIL_SIZE=0

count=$(getprop my.script.execution.count)

setprop rp.sata.storage.flag false
setprop rp.sata.rw.count no_sata
setprop rp.sata.storage.total 0
setprop rp.sata.storage.used 0

sata_flag=`cat /sys/class/ata_device/dev1.0/class`
echo $sata_flag > /dev/tty


if [ "$sata_flag" == "ata" ]; then
		echo "sata"
else
		echo "no sata"
		setprop rp.sata.rw.count no_sata
		exit 11
fi


storage_flag=`cat /sys/class/block/sda/removable`
echo $storage_flag > /dev/tty


if [ $storage_flag -eq "0" ]; then
		echo "sata"
		setprop rp.sata.storage.flag true
else
		echo "no sata"
		setprop rp.sata.storage.flag false
		setprop rp.sata.rw.count no_sata
		exit 11
fi


setprop rp.sata.storage.total 0
setprop rp.sata.storage.used 0
setprop rp.sata.rw.count 0

mountpoint=/mnt/sata

umount $mountpoint

mkdir $mountpoint


# 计算 /sys/class/block/ 目录下 sda 节点的数量
sda_count=$(ls /sys/class/block/ | grep -c ^sda)

# 如果只有一个 sda 节点
if [ $sda_count -eq 1 ]; then
    echo "只找到一个 sda 节点，将其挂载到 /mnt/udisk"
    mount /dev/block/sda $mountpoint
# 如果有两个 sda 节点
elif [ $sda_count -eq 2 ]; then
    echo "找到两个 sda 节点，将 sda1 挂载到 /mnt/udisk"
    mount /dev/block/sda1 $mountpoint
else
    echo "未找到 sda 节点，或者 sda 节点的数量超过两个"
fi


function get_emmc_avail_size()
{
	[  -d $OUT_PATH/rp_stress_path ]
  rm -Rf $OUT_PATH/rp_stress_path
	#for andriod, always output to /data
	AVAIL_SIZE=$(df | grep -w "$OUT_PATH" | awk '{print $4}')

	#for linux, if root > part, use root, otherwise use part
	if [ "X$AVAIL_SIZE" == "X" ];then
		
		root_size=$(df | grep -w "/" | awk '{print $4}')
		part_size=$(df | grep -w "mmcblk" | awk '{print $4}')

		if [ "X$part_size" == "X" ];then
			AVAIL_SIZE=$root_size
			return
		fi

		if [ $root_size -lt $part_size ];then
			OUT_PATH=$(df | grep -w "mmcblk" | awk '{print $6}')
			AVAIL_SIZE=$part_size
			echo "change OUT_PATH to $OUT_PATH"
		fi
	fi
}


function write_read_file()
{
	FILE_SIZE=$(($BLOCK_SIZE * $BLOCK_COUNT))
# Byte for MB
	FILE_SIZE=$(($FILE_SIZE / 1024 /1024))
# KB for MB
	AVAIL_SIZE=$(($AVAIL_SIZE / 1024))
	FILE_COUNT=$(((($AVAIL_SIZE / $FILE_SIZE) * 9) / 10))
    setprop my.script.execution.count 0
	[ ! -d $OUT_PATH/rp_stress_path ] && mkdir -p $OUT_PATH/rp_stress_path

	echo "AVAIL_SIZE: ${AVAIL_SIZE}M"
	echo "FILE_SIZE: ${FILE_SIZE}M"
	echo "FILE_COUNT: ${FILE_COUNT}"
	echo "OUT_PATH: ${OUT_PATH}"
	
	
	total=`df  $OUT_PATH | tail -1 | awk '{print $2 }' | cut -d'%' -f1`
	echo $total > /dev/tty
	setprop rp.sata.storage.total $total
	
	while [ 1 ];
	do
		loop=0
		while [ ${loop} -lt $FILE_COUNT ];
		do
		        for i in $(seq 1 10); do
		                echo "dd if=/dev/zero of=$OUT_PATH/rp_stress_path/file${loop} bs=${BLOCK_SIZE} count=$BLOCK_COUNT"
		                dd if=/dev/zero of=$OUT_PATH/rp_stress_path/file${loop} bs=${BLOCK_SIZE} count=$BLOCK_COUNT
		                sync && echo 3 > /proc/sys/vm/drop_caches
		                
		                FILE=$OUT_PATH/rp_stress_path/file${loop}
		                if [ -f "$FILE" ];then
		                	dd if=/$OUT_PATH/rp_stress_path/file${loop} of=/dev/zero
		                else
		                	setprop rp.sata.rw.count failed
		                	echo "failed" > /dev/tty
		                	exit 11
		                fi
		                
		                dd if=/$OUT_PATH/rp_stress_path/file${loop} of=/dev/zero
		                sync && echo 3 > /proc/sys/vm/drop_caches
		                let loop=${loop}+1
		                
		                used=`df  $OUT_PATH | tail -1 | awk '{print $3 }' | cut -d'%' -f1`
						        setprop rp.sata.storage.used $used
		        done
		done
        # 增加计数值
        count=$((count + 1))
       # 设置新的计数值
       setprop rp.sata.rw.count $count
        rm -rf $OUT_PATH/rp_stress_path/*
	done
}

get_emmc_avail_size
write_read_file


cd -

umount $mountpoint
rm -Rf $mountpoint
