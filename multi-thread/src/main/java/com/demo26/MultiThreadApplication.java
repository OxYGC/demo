package com.demo26;

import com.demo26.entity.Device;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class MultiThreadApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(MultiThreadApplication.class, args);
        // 获取本机IP（关键）
        InetAddress addr = InetAddress.getLocalHost();
        // 创建JmDNS实例
        JmDNS jmdns = JmDNS.create(addr);
        // 监听打印机
        discover(jmdns, "_ipp._tcp.local.");
        // 监听NAS / SMB
        discover(jmdns, "_smb._tcp.local.");
        // 监听HTTP服务（很多设备会暴露Web UI）
        discover(jmdns, "_http._tcp.local.");

        System.out.println("Listening for services...");
    }

    public static void discover(JmDNS jmdns, String serviceType) {
        jmdns.addServiceListener(serviceType, new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent event) {
                // 触发解析
                jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {
                System.out.println("Service removed: " + event.getName());
            }

            Map<String, Device> deviceMap = new ConcurrentHashMap<>();

            @Override

            public void serviceResolved(ServiceEvent event) {

                ServiceInfo info = event.getInfo();

                String host = info.getServer();   // 主机名

                String name = info.getName();     // 设备名

                String type = info.getType();     // 服务类型

                int port = info.getPort();

                Device device = deviceMap.computeIfAbsent(host, k -> {
                    Device d = new Device();
                    d.setName(name);
                    return d;

                });

                // 更新IP

                for (InetAddress addr : info.getInetAddresses()) {

                    device.getIps().add(addr.getHostAddress());

                }

                // 更新服务（去重）

                device.getServices().put(type, port);

                // 打印当前聚合结果
                printDevices();
            }

            public void printDevices() {
                System.out.println("\n========= Device List =========");
                for (Map.Entry<String, Device> entry : deviceMap.entrySet()) {
                    String host = entry.getKey();
                    Device device = entry.getValue();

                    System.out.println("Host: " + host + " (" + String.join(",", device.getIps()) + ")");
                    System.out.println(" ├─ Name: " + device.getName());
                    System.out.println(" ├─ Services:");

                    for (Map.Entry<String, Integer> svc : device.getServices().entrySet()) {
                        System.out.println(" │   ├─ " + svc.getKey() + " -> " + svc.getValue());
                    }
                    System.out.println();
                }
                System.out.println("================================\n");
            }
        });

        /**
         * 优化根据不同的Name和Host 进行分组打印，这样就能更加清晰一个host主机下有哪些暴露端口,以及协议类型。目前打印的不够简洁和规整
         */

    }




}
