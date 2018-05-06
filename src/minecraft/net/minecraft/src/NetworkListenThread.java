package net.minecraft.src;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.MinecraftServer;

public abstract class NetworkListenThread
{
    /** Reference to the MinecraftServer object. */
    private final MinecraftServer mcServer;
    private final List connections = Collections.synchronizedList(new ArrayList());

    /** Whether the network listener object is listening. */
    public volatile boolean isListening = false;

    public NetworkListenThread(MinecraftServer par1MinecraftServer) throws IOException
    {
        this.mcServer = par1MinecraftServer;
        this.isListening = true;
    }

    /**
     * adds this connection to the list of currently connected players
     */
    public void addPlayer(NetServerHandler par1NetServerHandler)
    {
      //DNCode Start
      //this.connections.add(par1NetServerHandler);
      this.connections.add(new BTHNetServerHandler(this.getServer(), par1NetServerHandler.netManager, par1NetServerHandler.playerEntity));
      //DNCode End
    }

    public void stopListening()
    {
        this.isListening = false;
    }

    /**
     * processes packets and pending connections
     */
    public void networkTick()
    {
        for (int var1 = 0; var1 < this.connections.size(); ++var1)
        {
            NetServerHandler var2 = (NetServerHandler)this.connections.get(var1);

            try
            {
                var2.networkTick();
            }
            catch (Exception var5)
            {
                if (var2.netManager instanceof MemoryConnection)
                {
                    CrashReport var4 = CrashReport.makeCrashReport(var5, "Ticking memory connection");
                    throw new ReportedException(var4);
                }

                this.mcServer.getLogAgent().logWarningException("Failed to handle packet for " + var2.playerEntity.getEntityName() + "/" + var2.playerEntity.getPlayerIP() + ": " + var5, var5);
                var2.kickPlayerFromServer("Internal server error");
            }

            if (var2.connectionClosed)
            {
                this.connections.remove(var1--);
            }

            var2.netManager.wakeThreads();
        }
    }

    public MinecraftServer getServer()
    {
        return this.mcServer;
    }
}