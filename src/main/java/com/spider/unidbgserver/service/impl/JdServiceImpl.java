package com.spider.unidbgserver.service.impl;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.linux.android.dvm.DvmClass;
import com.github.unidbg.linux.android.dvm.DvmObject;
import com.github.unidbg.linux.android.dvm.StringObject;
import com.github.unidbg.linux.android.dvm.VM;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.spider.unidbgserver.service.JdService;
import com.spider.unidbgserver.vm.JDVm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static cn.hutool.core.convert.Convert.hexToBytes;

@Service
public class JdServiceImpl implements JdService {

    @Autowired
    JDVm jdVm;
    String strCurJniKey = "";

    @Override
    public String getSign(String functionId, String uuid, String body) {
        VM vm = jdVm.getVm();
        AndroidEmulator emulator = jdVm.getEmulator();
        DvmClass cBitmapkitUtils = vm.resolveClass("com/jingdong/common/utils/BitmapkitUtils");
        StringObject ret = cBitmapkitUtils.callStaticJniMethodObject(emulator, "getSignFromJni()(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                vm.resolveClass("android/content/Context").newObject(null),
                functionId,
                body,
                uuid,
                "android",
                "15.1.55");
        return ret.getValue();
    }

    @Override
    public String getLoginBody(byte[] input) {
        VM vm = jdVm.getVm();
        AndroidEmulator emulator = jdVm.getEmulator();

        DvmClass DecryptorJni = vm.resolveClass("jd/wjlogin_sdk/util/DecryptorJni");
        StringObject randomKey = (StringObject) DecryptorJni.callStaticJniMethodObject((Emulator) emulator,
                "jniRandomKey()Ljava/lang/String;");
        strCurJniKey = randomKey.getValue();
        int length = input.length;
        String key = randomKey.getValue();

        DvmObject<?> result =  DecryptorJni.callStaticJniMethodObject((Emulator) emulator,
                "jniEncryptMsg([BILjava/lang/String;)[B",
                input, length, key);
        if (result != null) {
            System.out.println(" ----------------------- DecryptorJni.jniEncryptMsg returned: " + result);
        } else {
            System.out.println(" ----------------------- DecryptorJni.jniEncryptMsg returned: null");
        }

        byte[] output = (byte[]) result.getValue();  // result is DvmObject<byte[]>
        System.err.println("jniEncryptMsg Encrypted result: " + new String(output, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : output) {
            sb.append(String.format("%02x", b));
        }
        
        String base64Encoded = Base64.getEncoder().encodeToString(output);
        System.err.println("jniEncryptMsg Base64: " + base64Encoded);

        return base64Encoded;
    }

    @Override
    public String getJniDecryptMsg(byte[] response) {
        VM vm = jdVm.getVm();
        AndroidEmulator emulator = jdVm.getEmulator();

        DvmClass DecryptorJni = vm.resolveClass("jd/wjlogin_sdk/util/DecryptorJni");
        if( strCurJniKey == null || strCurJniKey.length() == 0 )
        {
            return "";
        }

        int length = response.length;
        String key = strCurJniKey;

        DvmObject<?> result =  DecryptorJni.callStaticJniMethodObject((Emulator) emulator,
                "jniDecryptMsg([BILjava/lang/String;)[B",
                response, length, key);
        if (result != null) {
            System.out.println(" ----------------------- DecryptorJni.jniDecryptMsg returned: " + result);
        } else {
            System.out.println(" ----------------------- DecryptorJni.jniDecryptMsg returned: null");
        }

        byte[] output = (byte[]) result.getValue();
        System.err.println("jniDecryptMsg result: " + new String(output, StandardCharsets.UTF_8));
        StringBuilder builder1 = new StringBuilder();
        for (byte b : output) {
            builder1.append(String.format("%02x", b));
        }
        System.err.println("Decrypted Hex: " + builder1.toString());

        return builder1.toString();
    }

    public String initJdguard() {
        VM vm = jdVm.getVm();
        AndroidEmulator emulator = jdVm.getEmulator();

        DvmObject<?>[] args = new DvmObject<?>[] {
                new StringObject(vm, "0"),
        };
        ArrayObject arrayObj = new ArrayObject(args);

        DvmClass Bridge = vm.resolveClass("com/jd/security/jdguard/core/Bridge");

        ArrayObject result =  Bridge.callStaticJniMethodObject((Emulator) emulator,
                "main(I[Ljava/lang/Object;)[Ljava/lang/Object;",
                103, arrayObj);
        if (result != null) {
            DvmObject<?>[] resultArray = result.getValue();
            Object[] output = new Object[resultArray.length];
            for (int i = 0; i < resultArray.length; i++) {
                output[i] = resultArray[i].getValue();
            }
            System.err.println(" ----------------------- Bridge.init returned: " + String.valueOf(output[0]));
            return String.valueOf(output[0]);
        }
        return "";
    }

    @Override
    public String getJdsgParams(String uri, String param, String eid, String version, String unknown) {
        initJdguard();

        VM vm = jdVm.getVm();
        AndroidEmulator emulator = jdVm.getEmulator();

        DvmClass Bridge = vm.resolveClass("com/jd/security/jdguard/core/Bridge");
        DvmClass integerClass = vm.resolveClass("java/lang/Integer");

        DvmObject<?>[] args = new DvmObject<?>[] {
                new ByteArray(vm, uri.getBytes(StandardCharsets.UTF_8)),
                new StringObject(vm, param),
                new StringObject(vm, eid),
                new StringObject(vm, version),
                new StringObject(vm, unknown),
                null,
                null
        };

        DvmObject<?>[] mock_args = new DvmObject<?>[] {
                new StringObject(vm, "1"),
        };

        ArrayObject arrayObj = new ArrayObject(args);

        ArrayObject result =  Bridge.callStaticJniMethodObject((Emulator) emulator,
                "main(I[Ljava/lang/Object;)[Ljava/lang/Object;",
                101, arrayObj);
        if (result != null) {
            DvmObject<?>[] resultArray = result.getValue();
            Object[] output = new Object[resultArray.length];
            for (int i = 0; i < resultArray.length; i++) {
                output[i] = resultArray[i].getValue();
            }
            System.err.println(" ----------------------- checkGuard1 returned: " + String.valueOf(output[0]));
            return String.valueOf(output[0]);
        }
        return "";
    }
}
