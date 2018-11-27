/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.template.util;

/**
 *
 * @author sntrk
 */
public class CharSequenceBuffer implements CharSequence {

	private char[] buffer;
	private int offset;
	private int length;
	
	public CharSequenceBuffer( char[] buffer, int length, int offset ) {
		this.buffer = buffer;
		this.length = length;
		this.offset = offset;
	}
	
	@Override
	public int length() {
		return this.length;
	}

	@Override
	public char charAt(int index) {
		return buffer[ offset + index ];
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return new CharSequenceBuffer(buffer, end - start + 1, offset + start );
	}
	
	@Override
	public String toString() {
		return new String( java.util.Arrays.copyOfRange(buffer, offset, offset+length-1));
	}
	
}
