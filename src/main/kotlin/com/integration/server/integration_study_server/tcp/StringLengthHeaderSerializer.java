package com.integration.server.integration_study_server.tcp;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

