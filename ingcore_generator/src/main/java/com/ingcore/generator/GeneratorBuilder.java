package com.ingcore.generator;


import com.ingcore.generator.build.BuilderClassManager;
import com.ingcore.generator.plugin.EclipseShellCallback;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.VerboseProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.util.messages.Messages;
import org.mybatis.generator.logging.LogFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

public class GeneratorBuilder {

    public static void main(String[] args) throws SAXException {

        InputStream is = System.in;
        Scanner scan = new Scanner(is);
        System.out.println("Are your sure run this ? ");
        System.out.println("Please enter the [yes] to continue [exit] quit .");
        String key = null;
        while (true) {
            key = scan.nextLine();
            if (key.equals("yes")) {
                break;
            } else if (key.equals("exit")) {
                System.exit(0);
            }
        }

        String xml = "./generatorConfig.xml";
        File configFile = new File(GeneratorBuilder.class.getClassLoader().getResource(xml).getFile());

        args = new String[2];
        args[0] = "-configfile";
        args[1] = configFile.getAbsolutePath();
        if (args.length == 0) {
            usage();
            System.exit(0);
            return;
        }
        Map arguments = parseCommandLine(args);
        if (arguments.containsKey("-?")) {
            usage();
            System.exit(0);
            return;
        }
        if (!arguments.containsKey("-configfile")) {
            writeLine(Messages.getString("RuntimeError.0"));
            return;
        }
        List<String> warnings = new ArrayList<String>();
        String configfile = (String) arguments.get("-configfile");
        File configurationFile = new File(configfile);
        if (!configurationFile.exists()) {
            writeLine(Messages.getString("RuntimeError.1", configfile));
            return;
        }

        Set fullyqualifiedTables = new HashSet();
        if (arguments.containsKey("-tables")) {
            StringTokenizer st = new StringTokenizer(
                    (String) arguments.get("-tables"), ",");
            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                if (s.length() > 0) {
                    fullyqualifiedTables.add(s);
                }
            }
        }

        Set contexts = new HashSet();
        if (arguments.containsKey("-contextids")) {
            StringTokenizer st = new StringTokenizer(
                    (String) arguments.get("-contextids"), ",");

            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                if (s.length() > 0) {
                    contexts.add(s);
                }
            }
        }
        try {
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(configurationFile);

//            EclipseShellCallback shellCallback = new EclipseShellCallback(
//                    arguments.containsKey("-overwrite"));
            EclipseShellCallback shellCallback = new EclipseShellCallback(true);

            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config,
                    shellCallback, warnings);
            ProgressCallback progressCallback = arguments
                    .containsKey("-verbose") ? new VerboseProgressCallback()
                    : null;
            myBatisGenerator.generate(progressCallback, contexts,
                    fullyqualifiedTables);

            BuilderClassManager bud = new BuilderClassManager();
            bud.render();
            System.out.println("Builder api successfully !");

        } catch (XMLParserException e) {
            writeLine(Messages.getString("Progress.3"));
            writeLine();
            for (String error : e.getErrors()) {
                writeLine(error);
            }

            return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (InvalidConfigurationException e) {
            writeLine(Messages.getString("Progress.16"));
            for (String error : e.getErrors()) {
                writeLine(error);
            }
            return;
        } catch (InterruptedException e) {
        }

        for (String warning : warnings) {
            writeLine(warning);
        }

        if (warnings.size() == 0) {
            writeLine(Messages.getString("Progress.4"));
        } else {
            writeLine();
            writeLine(Messages.getString("Progress.5"));
        }
    }

    private static void usage() {
        String lines = Messages.getString("Usage.Lines");
        int iLines = Integer.parseInt(lines);
        for (int i = 0; i < iLines; i++) {
            String key = "Usage." + i;
            writeLine(Messages.getString(key));
        }
    }

    private static void writeLine(String message) {
        System.out.println(message);
    }

    private static void writeLine() {
        System.out.println();
    }

    private static Map<String, String> parseCommandLine(String[] args) {
        List<String> errors = new ArrayList();
        Map arguments = new HashMap();

        for (int i = 0; i < args.length; i++) {
            if ("-configfile".equalsIgnoreCase(args[i])) {
                if (i + 1 < args.length)
                    arguments.put("-configfile", args[(i + 1)]);
                else {
                    errors.add(Messages.getString("RuntimeError.19",
                            "-configfile"));
                }

                i++;
            } else if ("-overwrite".equalsIgnoreCase(args[i])) {
                arguments.put("-overwrite", "Y");
            } else if ("-verbose".equalsIgnoreCase(args[i])) {
                arguments.put("-verbose", "Y");
            } else if ("-?".equalsIgnoreCase(args[i])) {
                arguments.put("-?", "Y");
            } else if ("-h".equalsIgnoreCase(args[i])) {
                arguments.put("-?", "Y");
            } else if ("-forceJavaLogging".equalsIgnoreCase(args[i])) {
                LogFactory.forceJavaLogging();
            } else if ("-contextids".equalsIgnoreCase(args[i])) {
                if (i + 1 < args.length)
                    arguments.put("-contextids", args[(i + 1)]);
                else {
                    errors.add(Messages.getString("RuntimeError.19",
                            "-contextids"));
                }

                i++;
            } else if ("-tables".equalsIgnoreCase(args[i])) {
                if (i + 1 < args.length)
                    arguments.put("-tables", args[(i + 1)]);
                else {
                    errors.add(Messages.getString("RuntimeError.19", "-tables"));
                }
                i++;
            } else {
                errors.add(Messages.getString("RuntimeError.20", args[i]));
            }
        }

        if (!errors.isEmpty()) {
            for (String error : errors) {
                writeLine(error);
            }

            System.exit(-1);
        }

        return arguments;
    }
}