/*-
 * #%L
 * rapidoid-commons
 * %%
 * Copyright (C) 2014 - 2020 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.rapidoid.util;

import org.rapidoid.RapidoidThing;
import org.rapidoid.annotation.*;
import org.rapidoid.cache.Caching;
import org.rapidoid.cls.Cls;
import org.rapidoid.collection.Coll;
import org.rapidoid.commons.Str;
import org.rapidoid.commons.URIs;
import org.rapidoid.config.Conf;
import org.rapidoid.config.Config;
import org.rapidoid.config.ConfigOptions;
import org.rapidoid.crypto.Crypto;
import org.rapidoid.ctx.Ctx;
import org.rapidoid.ctx.Ctxs;
import org.rapidoid.env.Env;
import org.rapidoid.event.Events;
import org.rapidoid.insight.Insights;
import org.rapidoid.io.IO;
import org.rapidoid.io.Res;
import org.rapidoid.job.Jobs;
import org.rapidoid.lambda.Dynamic;
import org.rapidoid.lambda.Lmbd;
import org.rapidoid.lambda.Mapper;
import org.rapidoid.log.GlobalCfg;
import org.rapidoid.log.Log;
import org.rapidoid.thread.AbstractLoopThread;
import org.rapidoid.thread.RapidoidThread;
import org.rapidoid.thread.RapidoidThreadFactory;
import org.rapidoid.thread.RapidoidThreadLocals;
import org.rapidoid.u.U;
import org.rapidoid.validation.RapidoidValidationError;
import org.rapidoid.wrap.BoolWrap;
import org.rapidoid.writable.ReusableWritable;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Authors("Nikolche Mihajlovski")
@Since("2.0.0")
public class Msc extends RapidoidThing {

    private static final String SPECIAL_ARG_REGEX = "\\s*(.*?)\\s*(->|<-|:=|<=|=>|==)\\s*(.*?)\\s*";

    public static final String OS_NAME = System.getProperty("os.name");

    public static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(8,
            new RapidoidThreadFactory("utils", true));

    private static volatile MscState state = new MscState();

    private static void resetState() {
        state = new MscState();
    }

    public static final Mapper<Object, Object> TRANSFORM_TO_SIMPLE_CLASS_NAME = src -> {
        if (src == null) {
            return null;
        }

        if (src instanceof Class<?>) {
            return ((Class<?>) src).getSimpleName();
        } else {
            return src.getClass().getName() + "@" + System.identityHashCode(src);
        }
    };

    private Msc() {
    }

    public static byte[] serialize(Object value) {
        try {
            ReusableWritable output = new ReusableWritable();

            ObjectOutputStream out = new ObjectOutputStream(output);
            out.writeObject(value);
            output.close();

            return output.copy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object deserialize(byte[] buf) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf));
            Object obj = in.readObject();
            in.close();
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void serialize(Object value, ByteBuffer buf) {
        byte[] bytes = serialize(value);
        buf.putInt(bytes.length);
        buf.put(bytes);
    }

    public static Object deserialize(ByteBuffer buf) {
        int len = buf.getInt();
        byte[] bytes = new byte[len];
        buf.get(bytes);
        return deserialize(bytes);
    }

    public static String stackTraceOf(Throwable e) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(output));
        return output.toString();
    }

    public static short bytesToShort(String s) {
        ByteBuffer buf = Bufs.buf(s);
        U.must(buf.limit() == 2);
        return buf.getShort();
    }

    public static int bytesToInt(String s) {
        ByteBuffer buf = Bufs.buf(s);
        U.must(buf.limit() == 4);
        return buf.getInt();
    }

    public static long bytesToLong(String s) {
        ByteBuffer buf = Bufs.buf(s);
        U.must(buf.limit() == 8);
        return buf.getLong();
    }

    public static boolean waitInterruption(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.interrupted();
            return false;
        }
    }

    public static void joinThread(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    public static void benchmark(String name, int count, final Runnable runnable) {
        doBenchmark(name, count, iteration -> runnable.run(), false);
    }

    public static void benchmark(String name, int count, BenchmarkOperation operation) {
        doBenchmark(name, count, operation, false);
    }

    public static void doBenchmark(String name, int count, BenchmarkOperation operation, boolean silent) {
        long start = U.time();

        for (int i = 0; i < count; i++) {
            operation.run(i);
        }

        if (!silent) {
            benchmarkComplete(name, count, start);
        }
    }

    public static void benchmarkComplete(String name, int count, long startTime) {

        long end = U.time();
        long ms = end - startTime;

        if (ms == 0) {
            ms = 1;
        }

        double avg = ((double) count / (double) ms);

        String avgs;

        if (avg > 1) {
            if (avg < 1000) {
                avgs = Math.round(avg) + "K";
            } else {
                avgs = Math.round(avg / 100) / 10.0 + "M";
            }
        } else {
            avgs = Math.round(avg * 1000) + "";
        }

        String data = String.format("%s: %s in %s ms (%s/sec)", name, count, ms, avgs);

        Log.info(data + " | " + Insights.getCpuMemStats());
    }

    public static void benchmarkMT(int threadsN, final String name, final int count, final CountDownLatch outsideLatch,
                                   final BenchmarkOperation operation) {

        U.must(count % threadsN == 0, "The number of thread must be a factor of the total count!");
        final int countPerThread = count / threadsN;

        final CountDownLatch latch = outsideLatch != null ? outsideLatch : new CountDownLatch(threadsN);

        long time = U.time();

        final Ctx ctx = Ctxs.get();

        for (int i = 1; i <= threadsN; i++) {
            new RapidoidThread() {

                @Override
                public void run() {
                    Ctxs.attach(ctx != null ? ctx.span() : null);

                    try {
                        doBenchmark(name, countPerThread, operation, true);
                        if (outsideLatch == null) {
                            latch.countDown();
                        }

                    } finally {
                        if (ctx != null) {
                            Ctxs.close();
                        }
                    }
                }

            }.start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw U.rte(e);
        }

        benchmarkComplete("avg(" + name + ")", threadsN * countPerThread, time);
    }

    public static void benchmarkMT(int threadsN, final String name, final int count, final Runnable runnable) {
        benchmarkMT(threadsN, name, count, null, iteration -> runnable.run());
    }

    public static void benchmarkMT(int threadsN, final String name, final int count, final BenchmarkOperation operation) {
        benchmarkMT(threadsN, name, count, null, operation);
    }

    public static void startMeasure() {
        state.measureStart = U.time();
    }

    public static void endMeasure() {
        long delta = U.time() - state.measureStart;
        Log.info("Benchmark", "time", delta + " ms");
    }

    public static void endMeasure(String info) {
        long delta = U.time() - state.measureStart;
        Log.info("Benchmark", "info", info, "time", delta + " ms");
    }

    public static void endMeasure(long count, String info) {
        long delta = U.time() - state.measureStart;
        long freq = Math.round(1000 * (double) count / delta);
        Log.info("Benchmark", "performance", U.frmt("%s %s in %s ms (%s/sec)", count, info, delta, freq));
    }

    public static Throwable rootCause(Throwable e) {
        while (e.getCause() != null) {
            e = e.getCause();
        }
        return e;
    }

    public static String fillIn(String template, String placeholder, String value) {
        return template.replace("{{" + placeholder + "}}", value);
    }

    public static String fillIn(String template, Object... namesAndValues) {
        String text = template.toString();

        for (int i = 0; i < namesAndValues.length / 2; i++) {
            String placeholder = (String) namesAndValues[i * 2];
            String value = Cls.str(namesAndValues[i * 2 + 1]);

            text = fillIn(text, placeholder, value);
        }

        return text;
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> lowercase(Map<String, T> map) {
        Map<String, T> lower = U.map();

        for (Entry<String, T> e : map.entrySet()) {
            T val = e.getValue();
            if (val instanceof String) {
                val = (T) ((String) val).toLowerCase();
            }
            lower.put(e.getKey().toLowerCase(), val);
        }

        return lower;
    }

    public static void multiThreaded(int threadsN, final Mapper<Integer, Void> executable) {

        final CountDownLatch latch = new CountDownLatch(threadsN);

        for (int i = 1; i <= threadsN; i++) {
            final Integer n = i;
            new Thread(() -> {
                Lmbd.eval(executable, n);
                latch.countDown();
            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void multiThreaded(int threadsN, final Runnable executable) {
        multiThreaded(threadsN, n -> {
            executable.run();
            return null;
        });
    }

    public static void append(StringBuilder sb, String separator, String value) {
        if (sb.length() > 0) {
            sb.append(separator);
        }
        sb.append(value);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T serializable(Object value) {
        if (value == null || value instanceof Serializable) {
            return (T) value;
        } else {
            throw U.rte("Not serializable: " + value);
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> cast(Map<?, ?> map) {
        return (Map<K, V>) map;
    }

    public static RapidoidThread loop(final Runnable loop) {
        RapidoidThread thread = new AbstractLoopThread() {
            @Override
            protected void loop() {
                loop.run();
            }
        };

        thread.start();

        return thread;
    }

    public static byte[] toBytes(Object obj) {

        if (obj instanceof byte[]) {
            return (byte[]) obj;

        } else if (obj instanceof ByteBuffer) {
            return Bufs.buf2bytes((ByteBuffer) obj);

        } else if (obj instanceof InputStream) {
            return IO.loadBytes((InputStream) obj);

        } else if (obj instanceof File) {
            Res res = Res.from((File) obj);
            res.mustExist();
            return res.getBytes();

        } else if (obj instanceof Res) {
            Res res = (Res) obj;
            res.mustExist();
            return res.getBytes();

        } else {

            // this might be a Widget, so rendering it requires double toString:
//			U.str(obj); // 1. data binding and event processing
            return U.str(obj).getBytes(); // 2. actual rendering
        }
    }

    public static boolean exists(Callable<?> accessChain) {
        try {
            return accessChain != null && accessChain.call() != null;
        } catch (NullPointerException e) {
            return false;
        } catch (Exception e) {
            throw U.rte(e);
        }
    }

    public static String uri(String... parts) {
        return "/" + constructPath("/", false, false, parts);
    }

    public static String path(String... parts) {
        return constructPath(File.separator, true, false, parts);
    }

    private static String constructPath(String separator, boolean preserveFirstSegment, boolean uriEscape, String... parts) {
        String s = "";

        for (int i = 0; i < parts.length; i++) {
            String part = U.safe(parts[i]);

            // trim '/'s and '\'s
            if (!preserveFirstSegment || i > 0) {
                part = Str.triml(part, "/");
            }

            if (!preserveFirstSegment || part.length() > 1 || i > 0) {
                part = Str.trimr(part, "/");
                part = Str.trimr(part, "\\");
            }

            if (!U.isEmpty(part)) {
                if (!s.isEmpty() && !s.endsWith(separator)) {
                    s += separator;
                }

                if (uriEscape) part = URIs.urlEncode(part);

                s += part;
            }
        }

        return s;
    }

    public static String refinePath(String path) {
        boolean absolute = path.startsWith("/");
        path = path(path.split("/"));
        return absolute ? "/" + path : path;
    }

    public static int countNonNull(Object... values) {
        int n = 0;

        for (Object value : values) {
            if (value != null) {
                n++;
            }
        }

        return n;
    }

    @SuppressWarnings("unchecked")
    public static <T> T dynamic(final Class<T> targetInterface, final Dynamic dynamic) {
        final Object obj = new Object();

        InvocationHandler handler = (proxy, method, args) -> {

            if (method.getDeclaringClass().equals(Object.class)) {
                if (method.getName().equals("toString")) {
                    return targetInterface.getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
                }
                return method.invoke(obj, args);
            }

            return dynamic.call(method, U.safe(args));
        };

        return ((T) Proxy.newProxyInstance(targetInterface.getClassLoader(), new Class[]{targetInterface}, handler));
    }

    public static boolean withWatchModule() {
        return Cls.getClassIfExists("org.rapidoid.io.watch.Watch") != null;
    }

    public static void terminate(final int afterSeconds) {
        Log.warn("Terminating application in " + afterSeconds + " seconds...");
        new Thread(() -> {
            U.sleep(afterSeconds * 1000);
            terminate();
        }).start();
    }

    public static void terminateIfIdleFor(final int idleSeconds) {
        Log.warn("Will terminate if idle for " + idleSeconds + " seconds...");

        new Thread(() -> {
            while (!Thread.interrupted()) {
                U.sleep(500);
                long lastUsed = Usage.getLastAppUsedOn();
                long idleSec = (U.time() - lastUsed) / 1000;
                if (idleSec >= idleSeconds) {
                    Usage.touchLastAppUsedOn();
                    terminate();
                }
            }
        }).start();
    }

    public static void terminate() {
        Log.warn("Terminating application.");
        System.exit(0);
    }

    public static byte sbyte(int n) {
        return (byte) (n - 128);
    }

    public static int ubyte(byte b) {
        return b + 128;
    }

    public static void logSection(String msg) {
        Log.info("!" + Str.mul("-", msg.length()));
        Log.info(msg);
        Log.info("!" + Str.mul("-", msg.length()));
    }

    public static void logProperties(Properties props) {
        for (Entry<Object, Object> p : props.entrySet()) {
            Log.info("Hibernate property", String.valueOf(p.getKey()), p.getValue());
        }
    }

    public static boolean isValidationError(Throwable error) {
        return (error instanceof RapidoidValidationError);
    }

    public static <T> List<T> page(Iterable<T> items, int page, int pageSize) {
        return Coll.range(items, (page - 1) * pageSize, page * pageSize);
    }

    public static List<?> getPage(Iterable<?> items, int page, int pageSize, Integer size, BoolWrap isLastPage) {
        int pageFrom = Math.max((page - 1) * pageSize, 0);
        int pageTo = (page) * pageSize + 1;

        if (size != null) {
            pageTo = Math.min(pageTo, size);
        }

        List<?> range = U.list(Coll.range(items, pageFrom, pageTo));
        isLastPage.value = range.size() < pageSize + 1;

        if (!isLastPage.value && !range.isEmpty()) {
            range.remove(range.size() - 1);
        }

        return range; // 1 item extra, to test if there are more results
    }

    public static void invokeMain(Class<?> clazz, String[] args) {
        Method main = Cls.getMethod(clazz, "main", String[].class);

        U.must(main.getReturnType() == void.class);
        U.must(Modifier.isPublic(main.getModifiers()));
        U.must(Modifier.isStatic(main.getModifiers()));

        Cls.invokeStatic(main, new Object[]{args});
    }

    public static void filterAndInvokeMainClasses(Object[] beans, Set<Class<?>> invoked) {
        List<Class<?>> toInvoke = U.list();

        for (Object bean : beans) {
            U.notNull(bean, "bean");

            if (bean instanceof Class<?>) {
                Class<?> clazz = (Class<?>) bean;
                if (Cls.isAnnotated(clazz, Run.class) && !invoked.contains(clazz)) {
                    toInvoke.add(clazz);
                }
            }
        }

        invoked.addAll(toInvoke);

        for (Class<?> clazz : toInvoke) {
            Msc.logSection("Invoking @Run component: " + clazz.getName());
            String[] args = U.arrayOf(String.class, Env.args());
            Msc.invokeMain(clazz, args);
        }
    }

    public static String annotations(Class<? extends Annotation>[] annotations) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        if (annotations != null) {
            for (int i = 0; i < annotations.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("@");
                sb.append(annotations[i].getSimpleName());
            }
        }

        sb.append("]");
        return sb.toString();
    }

    public static String classes(List<Class<?>> classes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        if (classes != null) {
            for (int i = 0; i < classes.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(classes.get(i).getSimpleName());

                if (i >= 100) {
                    sb.append("...");
                    break;
                }
            }
        }

        sb.append("]");
        return sb.toString();
    }

    public static String classNames(List<String> classes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        if (classes != null) {
            for (int i = 0; i < classes.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(U.last(classes.get(i).split("\\.")));

                if (i >= 100) {
                    sb.append("...");
                    break;
                }
            }
        }

        sb.append("]");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> protectSensitiveInfo(Map<String, T> data, T replacement) {
        Map<String, T> copy = U.map();

        for (Map.Entry<String, T> e : data.entrySet()) {
            T value = e.getValue();

            String key = e.getKey().toLowerCase();

            if (value instanceof Map<?, ?>) {
                value = (T) protectSensitiveInfo((Map<String, T>) value, replacement);

            } else if (sensitiveKey(key)) {
                value = replacement;
            }

            copy.put(e.getKey(), value);
        }

        return copy;
    }

    public static boolean sensitiveKey(String key) {
        return key.contains("password") || key.contains("secret") || key.contains("token") || key.contains("private");
    }

    public static int processId() {
        return U.num(processName().split("@")[0]);
    }

    public static String processName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    public static boolean matchingProfile(Class<?> clazz) {
        Profiles profiles = clazz.getAnnotation(Profiles.class);
        return profiles == null || Env.hasAnyProfile(profiles.value());
    }

    public static boolean isInsideTest() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        for (StackTraceElement traceElement : trace) {
            String cls = traceElement.getClassName();

            if (cls.startsWith("org.junit.") || cls.startsWith("org.testng.")) {
                return true;
            }
        }

        return false;
    }

    public static Thread thread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName("Msc-thread-" + runnable.getClass());
        thread.start();
        return thread;
    }

    public static void reset() {
        Env.reset();
        Events.reset();
        Log.reset();
        Crypto.reset();
        Res.reset();
        Conf.reset();
        Jobs.reset();
        Env.reset();
        Caching.reset();

        Ctxs.reset();
        U.must(Ctxs.get() == null);

        resetState();
    }

    public static boolean isAscii(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 127) return false;
        }
        return true;
    }

    public static RapidoidThreadLocals locals() {
        return RapidoidThreadLocals.get();
    }

    public static boolean bootService(Config config, String service) {

        List<String> services = config.entry("services").list();

        for (String srvc : services) {
            U.must(ConfigOptions.SERVICE_NAMES.contains(srvc), "Unknown service: '%s'!", srvc);
        }

        return services.contains(service);
    }

    public static boolean dockerized() {
        return state.dockerized;
    }

    public static void dockerized(boolean dockerized) {
        state.dockerized = dockerized;
    }

    public static Object maybeMasked(Object value) {
        return GlobalCfg.uniformOutput() ? "<?>" : value;
    }

    public static synchronized String id() {
        if (state.uid == null) {
            state.uid = Conf.ROOT.entry("id").or(processName());
        }

        return state.uid;
    }

    public static Map<String, String> parseArgs(List<String> args) {
        Map<String, String> arguments = U.map();

        for (String arg : U.safe(args)) {
            if (!isSpecialArg(arg)) {

                String[] parts = arg.split("=", 2);
                U.must(parts.length == 2, "The argument '%s' doesn't have a key=value format!", arg);

                arguments.put(parts[0], parts[1]);

            } else {
                throw U.rte("Special arguments are not supported since v6.0. Found: " + arg);
            }
        }

        return arguments;
    }

    private static boolean isSpecialArg(String arg) {
        return arg.matches(SPECIAL_ARG_REGEX);
    }

    public static boolean isDev() {
        if (Env.isInitialized()) {
            return Env.dev();
        }

        return !Msc.isInsideTest() && Env.dev();
    }

    public static String fileSizeReadable(long size) {
        if (size < 1024) return size + " B";

        long sizeKB = Math.round(size / 1024.0);
        return sizeKB + " KB";
    }

    public static String fileSizeReadable(String filename) {
        return fileSizeReadable(new File(filename).length());
    }

    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buf = ByteBuffer.wrap(new byte[16]);

        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());

        return buf.array();
    }

    public static UUID bytesToUUID(byte[] bytes) {
        U.must(bytes.length == 16, "Expected 16 bytes, got: %s", bytes.length);
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return new UUID(buf.getLong(), buf.getLong());
    }

    public static <T> T normalOrHeavy(T normal, T heavy) {
        return GlobalCfg.is("RAPIDOID_TEST_HEAVY") ? heavy : normal;
    }

    public static Method getTestMethodIfExists() {
        Method method = null;

        for (StackTraceElement trc : Thread.currentThread().getStackTrace()) {
            try {
                Class<?> logCls = Class.forName(trc.getClassName());

                for (Method m : logCls.getMethods()) {
                    if (m.getName().equals(trc.getMethodName())) {
                        for (Annotation ann : m.getDeclaredAnnotations()) {
                            if (ann.annotationType().getSimpleName().equals("Test")) {
                                method = m;
                            }
                        }
                    }
                }

            } catch (Exception e) {
                // do nothing
            }
        }

        return method;
    }

    public static void setPlatform(boolean platform) {
        state.platform = platform;
    }

    public static boolean isPlatform() {
        return state.platform;
    }

    public static boolean isMavenBuild() {
        return state.mavenBuild;
    }

    public static void setMavenBuild(boolean mavenBuild) {
        state.mavenBuild = mavenBuild;
    }

    public static String errorMsg(Throwable error) {
        return getErrorCodeAndMsg(error).msg();
    }

    public static ErrCodeAndMsg getErrorCodeAndMsg(Throwable err) {
        Throwable cause = Msc.rootCause(err);

        int code;
        String defaultMsg;
        String msg = cause.getMessage();

        if (cause instanceof SecurityException) {
            code = 403;
            defaultMsg = "Access denied!";

        } else if (cause.getClass().getSimpleName().equals("NotFound")) {
            code = 404;
            defaultMsg = "The requested resource could not be found!";

        } else if (Msc.isValidationError(cause)) {
            code = 422;
            defaultMsg = "Validation error!";

        } else {
            code = 500;
            defaultMsg = "Internal server error!";
        }

        msg = U.or(msg, defaultMsg);
        return new ErrCodeAndMsg(code, msg);
    }

    public static String detectZipRoot(InputStream zip) {
        Set<String> roots = U.set();

        try {

            ZipInputStream zis = new ZipInputStream(zip);
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                if (ze.isDirectory()) {
                    String fileName = ze.getName();
                    String parentDir = fileName.split("/|\\\\")[0];
                    roots.add(parentDir);
                }

                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        } catch (IOException e) {
            throw U.rte(e);
        }

        return roots.size() == 1 ? U.single(roots) : null;
    }

    public static void unzip(InputStream zip, String destFolder) {
        try {
            File folder = new File(destFolder);
            folder.mkdirs();

            ZipInputStream zis = new ZipInputStream(zip);
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                if (!ze.isDirectory()) {
                    String fileName = ze.getName();

                    File newFile = new File(destFolder + File.separator + fileName);
                    newFile.getParentFile().mkdirs();

                    IO.save(newFile.getAbsolutePath(), IO.loadBytes(zis));
                }

                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        } catch (IOException e) {
            throw U.rte(e);
        }
    }

    public static void printRapidoidBanner() {
        U.print(IO.load("rapidoid.txt"));
    }

    public static boolean isSilent() {
        return isMavenBuild();
    }

    public static String specialUriPrefix() {
        return Msc.isPlatform() ? "/rapidoid/" : "/_";
    }

    public static String specialUri(String... suffixes) {
        String uri = uri(suffixes);
        String suffix = Str.triml(uri, '/');
        return specialUriPrefix() + suffix;
    }

    public static String semiSpecialUri(String... suffixes) {
        String uri = uri(suffixes);
        String suffix = Str.triml(uri, '/');
        return Msc.isPlatform() ? "/rapidoid/" + suffix : "/" + suffix;
    }

    public static String http() {
        return MscOpts.isTLSEnabled() ? "https" : "http";
    }

    public static String urlWithProtocol(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        } else {
            return Msc.http() + "://" + url;
        }
    }

    public static boolean isAppResource(String filename) {
        String name = new File(filename).getName();
        return !name.startsWith(".")
                && !filename.contains("/.")
                && !name.endsWith("~")
                && !filename.contains("/~")
                && !name.endsWith(".staged")
                && !name.contains("___jb_"); // Jetbrains temporary files
    }

    public static void sortByOrder(List<Method> methods) {
        methods.sort(new Comparator<Method>() {
            @Override
            public int compare(Method a, Method b) {
                return orderOf(a) - orderOf(b);
            }

            private int orderOf(Method m) {
                Order order = m.getAnnotation(Order.class);
                return order != null ? order.value() : 0;
            }
        });
    }
}
