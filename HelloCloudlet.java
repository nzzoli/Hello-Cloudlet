
package eu.mosaic_cloud.examples.hello;


import java.util.UUID;

import eu.mosaic_cloud.cloudlets.connectors.httpg.HttpgQueueRequestedCallbackArguments;
import eu.mosaic_cloud.cloudlets.connectors.httpg.IHttpgQueueConnectorFactory;
import eu.mosaic_cloud.cloudlets.core.CloudletCallbackArguments;
import eu.mosaic_cloud.cloudlets.core.CloudletCallbackCompletionArguments;
import eu.mosaic_cloud.cloudlets.core.ICallback;
import eu.mosaic_cloud.cloudlets.core.ICloudletController;
import eu.mosaic_cloud.cloudlets.tools.DefaultCloudletCallback;
import eu.mosaic_cloud.cloudlets.tools.DefaultHttpgQueueConnectorCallback;
import eu.mosaic_cloud.connectors.httpg.HttpgRequestMessage;
import eu.mosaic_cloud.connectors.httpg.HttpgResponseMessage;
import eu.mosaic_cloud.connectors.httpg.IHttpgQueueConnector;
import eu.mosaic_cloud.platform.core.configuration.ConfigurationIdentifier;
import eu.mosaic_cloud.platform.core.configuration.IConfiguration;
import eu.mosaic_cloud.platform.core.utils.PlainTextDataEncoder;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;


public class HelloCloudlet
  	extends DefaultCloudletCallback<HelloCloudlet.Context>
{
	@Override
	public CallbackCompletion<Void> destroy (final Context context, final CloudletCallbackArguments<Context> arguments)
	{
		return (ICallback.SUCCESS);
	}
	
	@Override
	public CallbackCompletion<Void> initialize (final Context context, final CloudletCallbackArguments<Context> arguments)
	{
		context.cloudlet = arguments.getCloudlet ();
		context.configuration = context.configuration;
		this.logger.info("Cloudlet sa initializat cu succes");
		context.identity = UUID.randomUUID ().toString ();
		context.cloudlet = arguments.getCloudlet ();
		final IConfiguration cloudletConfiguration = context.cloudlet.getConfiguration ();
		this.logger.info("Contextul a fost preluat cu succes");
		final IConfiguration gatewayConfiguration = cloudletConfiguration.spliceConfiguration (ConfigurationIdentifier.resolveAbsolute ("gateway"));
		this.logger.info("Configurare gateway ok");
		context.gateway = context.cloudlet.getConnectorFactory (IHttpgQueueConnectorFactory.class).create (gatewayConfiguration, String.class, PlainTextDataEncoder.DEFAULT_INSTANCE, String.class, PlainTextDataEncoder.DEFAULT_INSTANCE, new GatewayCallbacks (), context);
		if (context.gateway==null){
			this.logger.info("Factory connector a returnat null");
		}
		this.logger.info("Initializare gateway...OK");		
		return (context.gateway.initialize ());		
	}
	@Override
	public CallbackCompletion<Void> initializeSucceeded (final Context context, final CloudletCallbackCompletionArguments<Context> arguments)
	{
		this.logger.info ("A intrat in initializare cu succes");		
		return ICallback.SUCCESS;
	}
	@Override
	public CallbackCompletion<Void> initializeFailed (final Context context, final CloudletCallbackCompletionArguments<Context> arguments)
	{
		this.logger.info ("Intra in functia de initialize Failed");		
		return ICallback.SUCCESS;
	}
	
	public static class Context
	{
		ICloudletController<Context> cloudlet;
		IConfiguration configuration;
		IHttpgQueueConnector<String, String> gateway;
		String identity;
	}
	public static final class GatewayCallbacks
	extends DefaultHttpgQueueConnectorCallback<Context, String, String, Void>
{
@Override
public CallbackCompletion<Void> requested (final Context context, final HttpgQueueRequestedCallbackArguments<String> arguments)
{
	final HttpgRequestMessage<String> request = arguments.getRequest ();
	final StringBuilder responseBody = new StringBuilder ();
	responseBody.append (String.format ("Cloudlet: %s\n", context.identity));
	responseBody.append (String.format ("HTTP version: %s\n", request.version));
	responseBody.append (String.format ("HTTP method: %s\n", request.method));
	responseBody.append (String.format ("HTTP path: %s\n", request.path));
	if (request.body != null) {
		responseBody.append ("HTTP body:\n");
		responseBody.append (request.body);
	} else
		responseBody.append ("HTTP body: empty\n");
	final HttpgResponseMessage<String> response = HttpgResponseMessage.create200 (request, responseBody.toString ());
	context.gateway.respond (response);
	return (ICallback.SUCCESS);
}
}
}
