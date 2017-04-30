package com.nlt.mobileteam.wifidirect.controller;

import android.media.MediaFormat;
import android.util.Log;

import com.nlt.mobileteam.cinacore.BroadcastManager;
import com.nlt.mobileteam.cinacore.CinaCoreModule;
import com.nlt.mobileteam.cinacore.utils.Utils;
import com.nlt.mobileteam.wifidirect.WifiDirectCore;
import com.nlt.mobileteam.wifidirect.controller.chat.ChatManager;
import com.nlt.mobileteam.wifidirect.controller.chat.ChatManagerAssistant;
import com.nlt.mobileteam.wifidirect.controller.chat.ChatManagerChief;
import com.nlt.mobileteam.wifidirect.controller.chat.MediaManager;
import com.nlt.mobileteam.wifidirect.controller.socket.AbstractGroupOwnerSocketHandler;
import com.nlt.mobileteam.wifidirect.controller.socket.ClientSocketHandler;
import com.nlt.mobileteam.wifidirect.controller.socket.SocketHandler;
import com.nlt.mobileteam.wifidirect.utils.Callback;
import com.nlt.mobileteam.wifidirect.utils.exception.MessageControllerException;

import org.json.JSONObject;

import java.net.Socket;

import static com.nlt.mobileteam.cinacore.Action.COMM_ASSISTANT_DISCONNECTING;
import static com.nlt.mobileteam.cinacore.serializable.SerializableUtils.getBytesFromMediaFormat;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_MEDIA_FORMAT;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_PROJ_INFO;


public class CommunicationController {

    private static final String TAG = CommunicationController.class.getSimpleName();
    private static final boolean VERBOSE = true;
    private static final int MAX_ASSISTANTS_COUNT = 4;

    private volatile static CommunicationController instance;

    private volatile CommunicatorList<ChatManager> chatManagers;
    private volatile CommunicatorList<MediaManager> mediaManagers;
    private volatile CommunicatorList<ChatManager> pingPongManagers;
    private MediaManager clientMediaManager;
    private AbstractGroupOwnerSocketHandler serverSocketHandler;
    private ClientSocketHandler clientSocketHandler;

    private CommunicationController() {
        Socket socket = new Socket();
        initChatManagerCommunicationList(socket);
        initMediaManagerCommunicationList(socket);
        initPingPongManagerCommunicationList(socket);
        Utils.trySetupKeepAliveOptions(socket);
    }

    private void initMediaManagerCommunicationList(final Socket socket) {
        mediaManagers = new CommunicatorList<MediaManager>(WifiDirectCore.devicesCount) {
            @Override
            protected MediaManager getEmptyCommunicator() {
                return new MediaManager(socket, socket);
            }
        };
    }

    private void initChatManagerCommunicationList(final Socket socket) {
        chatManagers = new CommunicatorList<ChatManager>(WifiDirectCore.devicesCount) {
            @Override
            protected ChatManager getEmptyCommunicator() {
                return new ChatManager(socket) {
                    @Override
                    public void closeSocketConnection() {
                        //NOP
                    }

                    @Override
                    public void run() {
                        //NOP
                    }
                };
            }
        };
    }

    private void initPingPongManagerCommunicationList(final Socket socket) {
        pingPongManagers = new CommunicatorList<ChatManager>(WifiDirectCore.devicesCount) {
            @Override
            protected ChatManager getEmptyCommunicator() {
                return new ChatManager(socket) {
                    @Override
                    public void closeSocketConnection() {
                        //NOP
                    }

                    @Override
                    public void run() {
                        //NOP
                    }
                };
            }
        };
    }

    public static CommunicationController get() {
        if (instance == null) {
            synchronized (CommunicationController.class) {
                if (instance == null) {
                    instance = new CommunicationController();
                }
            }
        }
        return instance;
    }

    /**
     * For correct answer
     * This method should be called from Director only!
     *
     * @return false if one or more Assistants connected to DDP Director
     * otherwise return true
     */
    public static boolean isNoAssistantsConnected() {
        if (CinaCoreModule.USE_PINGER) {
            return (get().pingPongManagers == null || get().pingPongManagers.size() == 0);
        } else {
            return (get().chatManagers == null || get().chatManagers.size() == 0);
        }
    }

    /**
     * For correct answer
     * This method should be called from Director only!
     *
     * @return true only when last Assistant connected to DDP Director
     * otherwise return false
     */

    public static boolean isMaxAssistantsCountReached() {
        if (VERBOSE) Log.i(TAG, "pingPongManagers.size() = " + get().pingPongManagers.size() +
                " chatManagers.size = " + get().chatManagers.size() +
                " mediaManagers.size = " + get().mediaManagers.size());
        if (CinaCoreModule.USE_PINGER) {
            return (get().pingPongManagers != null &&
                    get().pingPongManagers.size() == MAX_ASSISTANTS_COUNT);
        } else {
            return (get().chatManagers != null &&
                    get().chatManagers.size() == MAX_ASSISTANTS_COUNT);
        }
    }

    public static int getAssistantsCount() {
        if (CinaCoreModule.USE_PINGER) {
            if (get().pingPongManagers == null) {
                return -1;
            } else {
                return get().pingPongManagers.size();
            }
        } else {
            if (get().chatManagers == null) {
                return -1;
            } else {
                return get().chatManagers.size();
            }
        }
    }

    /**
     * @return index of added chatManager in list
     */
    public int addMessageChatManager(ChatManager chatManager) {
        if (VERBOSE) Log.i(TAG, "addMessageChatManager, chatManager " + chatManager);
        return chatManagers.add(chatManager);
    }

    public int addMessageChatManager(ChatManager chatManager, int indexToInsert) {
        if (VERBOSE)
            Log.i(TAG, "addMessageChatManager, chatManager " +
                    chatManager + " indexToInsert = " + indexToInsert);
        return chatManagers.add(chatManager, indexToInsert);
    }

    public int getIndexByChatManager(ChatManager chatManager) {
        int index = -1;
        if (chatManagers != null && chatManagers.contains(chatManager)) {
            index = chatManagers.indexOf(chatManager);
        }
        if (VERBOSE) Log.d(TAG, "getIndexByChatManager, index = " + index);
        return index;
    }

    public int addPingPongChatManager(ChatManager pingPongChatManager) {
        if (VERBOSE)
            Log.i(TAG, "addPingPongChatManager, pingPongChatManager " + pingPongChatManager);
        int index = pingPongManagers.add(pingPongChatManager);
        //TODO: quick fix, refactor this class - move all managers to Device and operate with device instead of separate manager!!!
        //quick fix - Just to be sure index to insert contains empty
        // communicator, DO NOT DELETE until refactoring!!!
        ChatManager messageChatManager = chatManagers.remove(index);
        if (messageChatManager != null) {
            if (VERBOSE)
                Log.i(TAG, "addPingPongChatManager(), messageChatManager remove and closeSocketConnection by index " + index);
            messageChatManager.closeSocketConnection();
        }
        MediaManager removedMediaManager = removeMediaManager(index);
        if (removedMediaManager != null) {
            if (VERBOSE)
                Log.i(TAG, "addPingPongChatManager(), mediaManager remove and closeSocketConnection by index " + index);
            removedMediaManager.closeSocketConnection();
        }
        return index;
    }

    public void sendMessageToAll(Message message) {
        Log.i("SOCKET", "sending " + message.toString());
        for (ChatManager chatManager : chatManagers) {
            chatManager.write(message);
        }
    }

    public void sendMessageToAll(Message message, String value) {
        Log.i("SOCKET", "sending " + message.toString());
        for (ChatManager chatManager : chatManagers) {
            chatManager.write(message, value);
        }
    }

    public void sendMessageToAll(Message message, int value) {
        Log.i("SOCKET", "sending " + message.toString());
        for (ChatManager chatManager : chatManagers) {
            chatManager.write(message, value);
        }
    }

    public void sendMessageToAll(Message message, long value) {
        Log.i("SOCKET", "sending " + message.toString());
        for (ChatManager chatManager : chatManagers) {
            chatManager.write(message, value);
        }
    }

    public void sendMessageToAll(Message message, int length, byte[] value) {
        Log.i("SOCKET", "sending " + message.toString());
        for (ChatManager chatManager : chatManagers) {
            chatManager.write(message, length, value);
        }
    }

    public void sendMessage(int deviceIndex, Message message) throws MessageControllerException {
        if (chatManagers.isEmptyCommunicator(deviceIndex)) {
            throw new MessageControllerException(
                    "Connection with the assistant lost. deviceIndex = " + deviceIndex);
        }
        chatManagers.get(deviceIndex).write(message);
    }

    public void sendMessage(int deviceIndex, Message message, String value)
            throws MessageControllerException {
        if (chatManagers.isEmptyCommunicator(deviceIndex)) {
            throw new MessageControllerException(
                    "Connection with the assistant lost. deviceIndex = " + deviceIndex);
        }
        chatManagers.get(deviceIndex).write(message, value);
    }

    private void stopSocketHandlers() {
        if (serverSocketHandler != null && serverSocketHandler.isAlive()) {
            serverSocketHandler.stopServerSocketHandler();
            interruptSocketHandler(serverSocketHandler);
            serverSocketHandler = null;
        }

        if (clientSocketHandler != null && clientSocketHandler.isAlive()) {
            interruptSocketHandler(clientSocketHandler);
            clientSocketHandler = null;
        }
    }

    private void interruptSocketHandler(SocketHandler socketHandler) {
        while (!socketHandler.isInterrupted() && socketHandler.isAlive()) {
            socketHandler.interrupt();
            try {
                socketHandler.join(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeConnections() {
        closeConnections(null);
    }

    public void closeConnections(Callback callback) {
        closeChatManagers(chatManagers);
        closeMediaManagers();
        closeChatManagers(pingPongManagers);

        stopSocketHandlers();

        if (callback != null) {
            callback.apply();
        }
    }

    private void closeMediaManagers() {
        if (mediaManagers != null) {
            for (MediaManager mediaManager : mediaManagers) {
                if (!mediaManagers.isEmptyCommunicator(mediaManager)) {
                    mediaManager.closeSocketConnection();
                }
            }
            mediaManagers.clear();
        }
    }

    private void closeChatManagers(CommunicatorList<ChatManager> chatManagers) {
        if (chatManagers != null) {
            for (ChatManager chatManager : chatManagers) {
                if (!chatManagers.isEmptyCommunicator(chatManager)) {
                    chatManager.closeSocketConnection();
                }
            }
            chatManagers.clear();
        }
    }

    public AbstractGroupOwnerSocketHandler getServerSocketHandler() {
        return serverSocketHandler;
    }

    public void sendProjectInfo(JSONObject projectInfo) {
        for (int i = 0; i < chatManagers.size(); ++i) {
            ((ChatManagerChief) chatManagers.get(i))
                    .sendSessionInfo(MESSAGE_PROJ_INFO, projectInfo.toString());
        }
    }

    public void removeChatManager(ChatManager chatManager) {
        if (chatManagers.contains(chatManager)) {
            if (VERBOSE) Log.i(TAG, "removeChatManager");
            int indexToRemove = chatManagers.indexOf(chatManager);
            removeManagersAndCloseConnectionsByIndex(indexToRemove);
        }
    }

    public void removePingPongerManager(ChatManager chatManager) {
        if (pingPongManagers != null && pingPongManagers.contains(chatManager)) {
            if (VERBOSE) Log.i(TAG, "removePingPonger");
            int index = pingPongManagers.indexOf(chatManager);
            removeManagersAndCloseConnectionsByIndex(index);
        }
    }

    public void setServerSocketHandler(AbstractGroupOwnerSocketHandler serverSocketHandler) {
        this.serverSocketHandler = serverSocketHandler;
    }

    public int addMediaManager(MediaManager mediaManager) {
        if (VERBOSE) Log.i(TAG, "addMediaManager, mediaManager = " + mediaManager);
        return mediaManagers.add(mediaManager);
    }

    public int addMediaManager(MediaManager mediaManager, int indexToInsert) {
        if (VERBOSE)
            Log.i(TAG, "addMediaManager, mediaManager = " +
                    mediaManager + " indexToInsert = " + indexToInsert);
        return mediaManagers.add(mediaManager, indexToInsert);
    }

    public MediaManager getMediaManager(int deviceIndex) {
        return mediaManagers.get(deviceIndex);
    }

    public MediaManager getClientMediaManager() {
        return clientMediaManager;
    }

    public void setClientSocketHandler(ClientSocketHandler clientSocketHandler) {
        this.clientSocketHandler = clientSocketHandler;
    }

    public void initClientMediaManager() {
        clientMediaManager = clientSocketHandler.setupMediaSockets();
    }

    private MediaManager removeMediaManager(int index) {
        if (VERBOSE) Log.i(TAG, "removeMediaManager index = " + index);
        return mediaManagers.remove(index);
    }

    /**
     * The Method removes all necessary communicators (communication managers: pinger, chat manager, media manager)
     * and if removing was success (returned deleted communicator) close appropriate socket connection and sends
     * COMM_ASSISTANT_DISCONNECTING command to clean DDP Director UI for specified index
     * <p>
     * NOTE: index must be +1 because while command processing index is corrected (decreased), so correction +1 is needed here!!!
     *
     * @param index in CommunicatorList to delete
     *              <p>
     *              Throws IllegalArgumentException if index less than 0
     */
    public void removeManagersAndCloseConnectionsByIndex(int index) {
        Log.e(TAG, "removeManagersAndCloseConnectionsByIndex(), index = " + index);
        if (index >= 0) {
            boolean sendDisconnectingMassage = false;
            if (pingPongManagers != null && pingPongManagers.size() > 0) {
                ChatManager removedPingPonger = pingPongManagers.remove(index);
                if (removedPingPonger != null) {
                    sendDisconnectingMassage = true;
                    removedPingPonger.closeSocketConnection();
                    Log.e(TAG, "pinponger removed");
                }
            }
            ChatManager removedChatManager = chatManagers.remove(index);
            if (removedChatManager != null) {
                sendDisconnectingMassage = true;
                removedChatManager.closeSocketConnection();
                Log.e(TAG, "chat manager removed");
            }

            MediaManager removedMediaManager = removeMediaManager(index);
            if (removedMediaManager != null) {
                sendDisconnectingMassage = true;
                removedMediaManager.closeSocketConnection();
                Log.e(TAG, "media manager removed");
            }

            if (sendDisconnectingMassage) {
                BroadcastManager.get().sendInt(COMM_ASSISTANT_DISCONNECTING, index + 1);
            }
        } else {
            throw new IllegalArgumentException("Wrong index to remove, index = " + index);
        }
    }

    public void sendRecordedVideo(String recordedVideoPath) {
        for (ChatManager cm : chatManagers) {
            if (!chatManagers.isEmptyCommunicator(cm)) {
                ((ChatManagerAssistant) cm).sendVideoHader(recordedVideoPath);
            }
        }
    }

    public void sendMediaFormat(MediaFormat format) {
        assert format != null;

        byte[] serializedFormat = getBytesFromMediaFormat(format);
        sendMessageToAll(MESSAGE_MEDIA_FORMAT, serializedFormat.length, serializedFormat);
    }
}
