package org.hobbit.sdk.examples.examplebenchmark.system.container;

import com.google.gson.Gson;
import com.rabbitmq.client.MessageProperties;
import org.hobbit.core.components.Component;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.CommandSender;
import org.hobbit.sdk.utils.commandreactions.CommandReaction;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class MultipleCommandsReaction implements CommandReaction {

    private static final Logger logger = LoggerFactory.getLogger(MultipleCommandsReaction.class);
    private final ComponentsExecutor componentsExecutor;
    private final CommandQueueListener commandQueueListener;
    private boolean benchmarkReady = false;
    private boolean dataGenReady = false;
    private boolean taskGenReady = false;
    private boolean evalStorageReady = false;
    private boolean systemReady = false;
    private boolean startBenchmarkCommandSent = false;
    private Component dataGenerator;
    private Component taskGenerator;
    private Component evalStorage;
    private Component evalModule;
    private Component database;
    private Component factcheck;
    private String dataGeneratorImageName;
    private String taskGeneratorImageName;
    private String evalStorageImageName;
    private String evalModuleImageName;
    private String databaseImageName;
    private String factcheckImageName;
    private String dataGenContainerId;
    private String taskGenContainerId;
    private String systemContainerId;
    private String evalModuleContainerId;
    private String evalStorageContainerId;
    private Gson gson = new Gson();

    public MultipleCommandsReaction(ComponentsExecutor componentsExecutor, CommandQueueListener commandQueueListener) {
        this.componentsExecutor = componentsExecutor;
        this.commandQueueListener = commandQueueListener;
    }

    public MultipleCommandsReaction dataGenerator(Component component) {
        this.dataGenerator = component;
        return this;
    }

    public MultipleCommandsReaction dataGeneratorImageName(String value) {
        this.dataGeneratorImageName = value;
        return this;
    }

    public MultipleCommandsReaction taskGenerator(Component component) {
        this.taskGenerator = component;
        return this;
    }

    public MultipleCommandsReaction taskGeneratorImageName(String value) {
        this.taskGeneratorImageName = value;
        return this;
    }

    public MultipleCommandsReaction evalStorage(Component component) {
        this.evalStorage = component;
        return this;
    }

    public MultipleCommandsReaction evalStorageImageName(String value) {
        this.evalStorageImageName = value;
        return this;
    }

    public MultipleCommandsReaction systemContainerId(String value) {
        this.systemContainerId = value;
        return this;
    }

    public MultipleCommandsReaction evalModule(Component value) {
        this.evalModule = value;
        return this;
    }

    public MultipleCommandsReaction evalModuleImageName(String value) {
        this.evalModuleImageName = value;
        return this;
    }

    public MultipleCommandsReaction database(Component value) {
        this.database = value;
        return this;
    }

    public MultipleCommandsReaction databaseImageName(String value) {
        this.databaseImageName = value;
        return this;
    }

    public MultipleCommandsReaction factcheck(Component value) {
        this.factcheck = value;
        return this;
    }

    public MultipleCommandsReaction factcheckImageName(String value) {
        this.factcheckImageName = value;
        return this;
    }

    public void handleCmd(Byte command, byte[] bytes, String replyTo) throws Exception {
        if (this.systemContainerId == null) {
            throw new Exception("SystemContainerId not specified. Impossible to continue");
        } else {

            String containerId;
            if (command == 12) {
                String dataString = RabbitMQUtils.readString(bytes);
                StartCommandData startCommandData = (StartCommandData) this.gson.fromJson(dataString, StartCommandData.class);
                logger.debug("CONTAINER_START signal received with imageName=" + startCommandData.image + "");
                Component compToSubmit = null;
                containerId = null;
                if (this.dataGenerator != null && startCommandData.image.equals(this.dataGeneratorImageName)) {
                    compToSubmit = this.dataGenerator;
                    containerId = this.dataGeneratorImageName;
                }

                if (this.taskGenerator != null && startCommandData.image.equals(this.taskGeneratorImageName)) {
                    compToSubmit = this.taskGenerator;
                    containerId = this.taskGeneratorImageName;
                }

                if (this.evalStorage != null && startCommandData.image.equals(this.evalStorageImageName)) {
                    compToSubmit = this.evalStorage;
                    containerId = this.evalStorageImageName;
                }

                if (this.evalModule != null && startCommandData.image.equals(this.evalModuleImageName)) {
                    compToSubmit = this.evalModule;
                    containerId = this.evalModuleImageName;
                }

                if (this.database != null && startCommandData.image.equals(this.databaseImageName)) {
                    compToSubmit = this.database;
                    containerId = this.databaseImageName;
                }

                if (this.factcheck != null && startCommandData.image.equals(this.factcheckImageName)) {
                    compToSubmit = this.factcheck;
                    containerId = this.factcheckImageName;
                }


                if (compToSubmit == null) {
                    throw new Exception("No component to start as imageName=" + startCommandData.image);
                }

                this.componentsExecutor.submit(compToSubmit, containerId, startCommandData.getEnvironmentVariables());
                synchronized (this) {
                    if (containerId != null) {
                        try {
                            (new CommandSender(containerId.getBytes(), MessageProperties.PERSISTENT_BASIC, replyTo)).send();
                        } catch (Exception var17) {
                            Assert.fail(var17.getMessage());
                        }
                    } else {
                        String var9 = "123";
                    }
                }
            }

            if (command == 16) {
                CommandSender commandSender = null;
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                String containerName = RabbitMQUtils.readString(buffer);
                if (containerName.equals(this.systemContainerId)) {
                    containerId = "123";
                }

                if (containerName.equals(this.dataGeneratorImageName)) {
                    commandSender = new CommandSender((byte) 14);
                }

                if (containerName.equals(this.taskGeneratorImageName)) {
                    commandSender = new CommandSender((byte) 15);
                }

                synchronized (this) {
                    if (commandSender != null) {
                        try {
                            commandSender.send();
                        } catch (Exception var15) {
                            logger.error(var15.getMessage());
                        }
                    }
                }
            }

            if (command == 11) {
                try {
                    this.commandQueueListener.terminate();
                    this.componentsExecutor.shutdown();
                } catch (InterruptedException var14) {
                    System.out.println(var14.getMessage());
                }
            }

            if (command == 2) {
                this.benchmarkReady = true;
            }

            if (command == 3) {
                this.dataGenReady = true;
            }

            if (command == 4) {
                this.taskGenReady = true;
            }

            if (command == 5) {
                this.evalStorageReady = true;
            }

            if (command == 1) {
                this.systemReady = true;
            }

            synchronized (this) {
                if (this.benchmarkReady && (this.dataGenerator == null || this.dataGenReady) && (this.taskGenerator == null || this.taskGenReady) && (this.evalStorage == null || this.evalStorageReady) && this.systemReady && !this.startBenchmarkCommandSent) {
                    this.startBenchmarkCommandSent = true;

                    try {
                        (new CommandSender((byte) 17, this.systemContainerId)).send();
                    } catch (Exception var13) {
                        logger.error(var13.getMessage());
                    }
                }

            }
        }
    }
}
