package com.spider.unidbgserver.jni;

import com.github.unidbg.Emulator;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.linux.LinuxFileSystem;
import com.github.unidbg.linux.android.AndroidARM64Emulator;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.linux.android.dvm.jni.ProxyDvmObject;
import com.github.unidbg.linux.android.dvm.wrapper.DvmInteger;

import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.util.Store;
import org.bouncycastle.cert.X509CertificateHolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JDJni extends AbstractJni {

    private static final String APK_PATH = "/data/app/com.jingdong.app.mall.apk";

    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature) {
            case "com/jingdong/common/utils/BitmapkitUtils->a:Landroid/app/Application;": {
                return vm.resolveClass("android/app/Activity", vm.resolveClass("android/content/ContextWrapper", vm.resolveClass("android/content/Context"))).newObject(null);
            }
            case "android/provider/Settings$Secure->ANDROID_ID:Ljava/lang/String;": {
                return new StringObject(vm, "d7d88e68f3599478");
            }
        }
        return super.getStaticObjectField(vm, dvmClass, signature);
    }

    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, DvmMethod dvmMethod, VaList vaList) {
        switch (dvmMethod.getSignature()) {
            case "com/jd/security/jdguard/core/Bridge->getAppContext()Landroid/content/Context;": {
                DvmClass contextClass = vm.resolveClass("android/content/Context");
                return contextClass.newObject(null);
            }
            case "com/jd/security/jdguard/core/Bridge->getAppKey()Ljava/lang/String;": {
                return new StringObject(vm, "852aa6ad-a1e4-4ceb-913c-96f3150a2d9d");
            }
            case "com/jd/security/jdguard/core/Bridge->getJDGVN()Ljava/lang/String;": {
                return new StringObject(vm, "3.3.6");
            }
            case "com/jd/security/jdguard/core/Bridge->getBizNum(Ljava/lang/String;)Ljava/lang/String;": {
                DvmObject<?> arg = vaList.getObjectArg(0);
                String input = arg.getValue().toString();
                if ("srvTk".equals(input)) {
                    return new StringObject(vm, "0");
                }
                if ("timeline".equals(input)) {
                    return new StringObject(vm, "1");
                }
            }
            case "android/provider/Settings$Secure->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;": {
                DvmObject<?> arg = vaList.getObjectArg(1);
                String input = arg.getValue().toString();
                if ("android_id".equals(input)) {
                    return new StringObject(vm, "d7d88e68f3599478");
                }
                else {
                    System.err.println("Input Key: " + input);
                    return new StringObject(vm, input);
                }
            }
        }
        return super.callStaticObjectMethodV(vm, dvmClass, dvmMethod, vaList);
    }

    @Override
    public DvmObject<?> getObjectField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
        switch (signature) {
            case "android/content/pm/ApplicationInfo->sourceDir:Ljava/lang/String;": {
                return new StringObject(vm, APK_PATH);
            }
        }
        return super.getObjectField(vm, dvmObject, signature);
    }

    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        switch (signature) {
            case "com/jingdong/common/utils/BitmapkitZip->unZip(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)[B": {
                StringObject apkPath = varArg.getObjectArg(0);
                StringObject directory = varArg.getObjectArg(1);
                StringObject filename = varArg.getObjectArg(2);
                if (APK_PATH.equals(apkPath.getValue()) &&
                        "META-INF/".equals(directory.getValue()) &&
                        ".RSA".equals(filename.getValue())) {
                    byte[] data = vm.unzip("META-INF/JINGDONG.RSA");
                    return new ByteArray(vm, data);
                }
            }
            case "com/jingdong/common/utils/BitmapkitZip->objectToBytes(Ljava/lang/Object;)[B": {
                DvmObject<?> obj = varArg.getObjectArg(0);
                byte[] bytes = objectToBytes(obj.getValue());
                return new ByteArray(vm, bytes);
            }
        }
        return super.callStaticObjectMethod(vm ,dvmClass, signature, varArg);
    }

    @Override
    public DvmObject<?> newObject(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        switch (signature) {
            case "sun/security/pkcs/PKCS7-><init>([B)V": {
                ByteArray array = varArg.getObjectArg(0);
                try {
                    CMSSignedData cms = new CMSSignedData(array.getValue());
                    return vm.resolveClass("sun/security/pkcs/PKCS7").newObject(cms);
                } catch (CMSException e) {
                    throw new RuntimeException("Failed to create CMSSignedData from byte array", e);
                }
            }
        }
        return super.newObject(vm, dvmClass, signature, varArg);
    }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        switch (signature) {
            case "sun/security/pkcs/PKCS7->getCertificates()[Ljava/security/cert/X509Certificate;": {
                CMSSignedData cms = (CMSSignedData) dvmObject.getValue();
                try {
                    Store<X509CertificateHolder> certStore = cms.getCertificates();
                    Collection<X509CertificateHolder> certs = certStore.getMatches(null);
                    List<X509Certificate> x509List = new ArrayList<>();
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    for (X509CertificateHolder holder : certs) {
                        x509List.add((X509Certificate) certFactory.generateCertificate(
                                new java.io.ByteArrayInputStream(holder.getEncoded())));
                    }
                    return ProxyDvmObject.createObject(vm, x509List.toArray(new X509Certificate[0]));
                } catch (CertificateException | IOException e) {
                    throw new RuntimeException("Failed to process certificates from CMSSignedData", e);
                }
            }
        }
        return super.callObjectMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public DvmObject<?> newObjectV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature) {
            case "java/lang/StringBuffer-><init>()V": {
                return vm.resolveClass("java/lang/StringBuffer").newObject(new StringBuffer());
            }
            case "java/lang/Integer-><init>(I)V": {
                return DvmInteger.valueOf(vm, vaList.getIntArg(0));
            }
        }
        return super.newObjectV(vm, dvmClass, signature, vaList);
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "java/lang/StringBuffer->append(Ljava/lang/String;)Ljava/lang/StringBuffer;": {
                StringBuffer buffer = (StringBuffer) dvmObject.getValue();
                StringObject str = vaList.getObjectArg(0);
                buffer.append(str.getValue());
                return dvmObject;
            }
            case "java/lang/Integer->toString()Ljava/lang/String;": {
                return new StringObject(vm, ((Integer)dvmObject.getValue()).toString());
            }
            case "java/lang/StringBuffer->toString()Ljava/lang/String;": {
                return new StringObject(vm, ((StringBuffer)dvmObject.getValue()).toString());
            }
            case "android/content/Context->getContentResolver()Landroid/content/ContentResolver;": {
                DvmClass resolverClass = vm.resolveClass("android/content/ContentResolver");
                return resolverClass.newObject(null);
            }
            case "android/content/Context->getFilesDir()Ljava/io/File;": {
                DvmClass fileClass = vm.resolveClass("java/io/File");
                java.io.File file = new java.io.File("/files");
                return fileClass.newObject(file);
            }
        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }

    private static byte[] objectToBytes(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            byte[] array = baos.toByteArray();
            oos.close();
            baos.close();
            return array;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
