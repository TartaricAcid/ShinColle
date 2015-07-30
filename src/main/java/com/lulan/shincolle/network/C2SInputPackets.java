package com.lulan.shincolle.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.BasicEntityMount;
import com.lulan.shincolle.entity.BasicEntityShip;
import com.lulan.shincolle.entity.mounts.EntityMountSeat;
import com.lulan.shincolle.handler.FML_COMMON_EventHandler;
import com.lulan.shincolle.reference.ID;
import com.lulan.shincolle.utility.EntityHelper;
import com.lulan.shincolle.utility.LogHelper;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**CLIENT TO SERVER: KEY INPUT PACKETS
 * �Nclient�ݪ�����o�e��server
 */
public class C2SInputPackets implements IMessage {
	
	private World world;
	private EntityPlayer player;
	private int type, worldID, entityID, value, value2;
	
	
	public C2SInputPackets() {}	//�����n���ŰѼ�constructor, forge�~��ϥΦ�class
	
	//type 0: mount key input
	//type 1: sync current item
	public C2SInputPackets(int type, int value, int value2) {
        this.type = type;
        this.value = value;
        this.value2 = value2;
    }
	
	//type :
	public C2SInputPackets(int type, Entity entity, int value, int value2) {
        this.type = type;
        this.player = (EntityPlayer) entity;
        this.worldID = player.worldObj.provider.dimensionId;
        this.value = value;
        this.value2 = value2;
    }
	
	//����packet��k, server side
	@Override
	public void fromBytes(ByteBuf buf) {	
		//get type and entityID
		this.type = buf.readByte();
	
		switch(type) {
		case 0:	//ship entity gui click
		case 1:	//sync current item
			{
				this.value = buf.readInt();
				this.value2 = buf.readInt();
			}
			break;
		}
	}

	//�o�Xpacket��k, client side
	@Override
	public void toBytes(ByteBuf buf) {
		switch(this.type) {
		case 0:	//ship entity gui click
		case 1:	//sync current item
			{
				buf.writeByte((byte)this.type);
				buf.writeInt(this.value);
				buf.writeInt(this.value2);
			}
			break;
		}
	}
	
	//packet handler (inner class)
	public static class Handler implements IMessageHandler<C2SInputPackets, IMessage> {
		//����ʥ]�����debug�T��, server side
		@Override
		public IMessage onMessage(C2SInputPackets message, MessageContext ctx) {		
			EntityPlayerMP player = ctx.getServerHandler().playerEntity;
			
			switch(message.type) {
			case 0:	//mounts key input packet
				LogHelper.info(String.format("DEBUG : client key input: %s from %s", message.value, player.getDisplayName()));
				//set player's mount movement
				if(player.isRiding() && player.ridingEntity instanceof EntityMountSeat) {
					BasicEntityMount mount = ((EntityMountSeat)player.ridingEntity).host;
					
					if(mount != null) {
						BasicEntityShip ship = (BasicEntityShip) mount.getOwner();
						
						//check ship owner is player
						if(ship != null && EntityHelper.checkSameOwner(player, ship.getOwner())) {
							//set mount movement
							mount.keyPressed = message.value;
							
							//open ship GUI
							if(message.value2 == 1) {
								if(mount.getOwner() != null) {
									FMLNetworkHandler.openGui(player, ShinColle.instance, ID.G.SHIPINVENTORY, player.worldObj, mount.getOwner().getEntityId(), 0, 0);
								}
							}
						}
					}
				}
				break;
			case 1:	//sync current item
				player.inventory.currentItem = message.value;
				break;
			}//end switch
			
			return null;
		}
    }
	

}


