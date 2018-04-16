/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package io.shardingjdbc.proxy.frontend.netty;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.proxy.frontend.common.FrontendHandlerFactory;
import io.shardingjdbc.proxy.transport.common.codec.PacketCodecFactory;
import lombok.AllArgsConstructor;

/**
 * @author xiaoyu
 */
@AllArgsConstructor
public class NettyServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private EventLoopGroup userGroup;

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // TODO load database type from yaml or startup arguments
        pipeline.addLast(PacketCodecFactory.createPacketCodecInstance(DatabaseType.MySQL));
        pipeline.addLast(FrontendHandlerFactory
                .createFrontendHandlerInstance(DatabaseType.MySQL, userGroup));

    }
}
