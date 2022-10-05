package jmini3d.gwt;

/**
 * In GWT the resource load is async, so this is needed to force a redraw after a texture or
 * a shader is uploaded to the GPU
 */
public interface GpuUploaderListener {

	public void onGpuUploadFinish();

}