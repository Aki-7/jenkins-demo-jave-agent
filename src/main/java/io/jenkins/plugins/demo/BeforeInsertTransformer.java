package io.jenkins.plugins.demo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Insert code at the head of target Class#method
 */
public class BeforeInsertTransformer implements ClassFileTransformer {
  private final String targetClass;
  private final String targetMethod;
  private final String insertCode;

  public BeforeInsertTransformer(String clazz, String method, String code) {
    targetClass = clazz;
    targetMethod = method;
    insertCode = code;
  }

  /**
   * This is what I say something like metaprogramming.
   */
  public byte[] transform(
    ClassLoader loader,
    String className,
    Class<?> classBeingRedefined,
    ProtectionDomain protectionDomain,
    byte[] classfileBuffer
  ) throws IllegalClassFormatException {
    try {
      ByteArrayInputStream stream = new ByteArrayInputStream(classfileBuffer);
      CtClass ctClass = ClassPool.getDefault().makeClass(stream);

      if (!ctClass.getName().equals(targetClass)) return ctClass.toBytecode();

      CtMethod ctMethod = ctClass.getDeclaredMethod(targetMethod);
      ctMethod.insertBefore(insertCode);

      return ctClass.toBytecode();
    } catch (CannotCompileException | IOException | NotFoundException e) {
      e.printStackTrace();
      IllegalClassFormatException ecfe = new IllegalClassFormatException();
      ecfe.initCause(e);
      throw ecfe;
    }
  }
}
