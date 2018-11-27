/*
 * (c) 2018 hayde string buffer extended
 * this buffer similar to the StringBuffer of java. but it will grow in 
 * block chunks of 16 kb. And for using in match function, it uses the 
 * charsequenceext, which is made for nested requests on the string buffer
 */
package eu.hayde.box.template.util;

/**
 *
 * @author cn.sntrk
 */
public class StringBufferExt {
	
	private char[] buffer;
	private final int BLOCK_CHUNK = 1024*16;
	private int block_count = 0;
	private int length = 0;
	public  static int counter = 0;
	
	/**
	 * initialize the internal char buffer
	 * @param content 
	 */
	public StringBufferExt( String content ) {
		StringBufferExt.counter ++;
		
		char[] content_array = content.toCharArray();
		_resize_buffer( content_array.length );
		
		System.arraycopy(content_array, 0, buffer, 0, content_array.length);
		length = content_array.length;
	}
	
	public CharSequence get_charsequence() {
		CharSequence rv = new CharSequenceBuffer( buffer, length, 0);
		return rv;
	}
	
	public int indexOf( String search, int start_pos ) {
		int rv = -1;
		char[] search_char = search.toCharArray();
		int found = 0;
		int end_pos = length - start_pos;// + length;
		for( int i=start_pos; i<end_pos; i++ ) {
			if( buffer[i] == search_char[found] ) {
				found++;
				if( found==search_char.length) {
					rv = i-found;
					break;
				}
			} else {
				found = 0;
			}
		}
		return rv;
	}
	
	
	public void replace( int start, int end, String content ) {
		char[] content_char = content.toCharArray();
		int gap = end - start;
		if( content_char.length > gap ) {
			_resize_buffer( length + ( content_char.length - gap ) );
		}
		/**
		 * first copy the last part to the new place
		 */
		System.arraycopy(buffer, end, buffer, start + content_char.length, length-end);
		System.arraycopy(content_char, 0, buffer, start, content_char.length);
		this.length = length + ( content_char.length - gap );
	}
	
	public String substring( int start, int end ) {
		return new String( java.util.Arrays.copyOfRange(buffer, start, end) );
	}
	
	private void _resize_buffer( int estimated_length ) {
		int block_count_temp = ( estimated_length / BLOCK_CHUNK) + 1;
		if( block_count_temp > block_count ) {
			char[] buffer_temp = new char[ block_count_temp * BLOCK_CHUNK ];	
			if( buffer != null ) {
				System.arraycopy(buffer, 0, buffer_temp, 0, length);
			}
			buffer = buffer_temp;
			block_count = block_count_temp;
		}
	}
	
	@Override
	public String toString() {
		return new String( java.util.Arrays.copyOf(buffer, length));
	}
}
