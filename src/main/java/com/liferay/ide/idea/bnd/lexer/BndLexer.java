/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.idea.bnd.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import com.liferay.ide.idea.bnd.psi.BndTokenType;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dominik Marks
 */
public class BndLexer extends LexerBase {

	@Override
	public void advance() {
		_tokenStart = _tokenEnd;
		_parseNextToken();
	}

	@Override
	public int getBufferEnd() {
		return _endOffset;
	}

	@NotNull
	@Override
	public CharSequence getBufferSequence() {
		return _buffer;
	}

	@Override
	public int getState() {
		if (_defaultState) {
			return 0;
		}

		return 1;
	}

	@Override
	public int getTokenEnd() {
		return _tokenEnd;
	}

	@Override
	public int getTokenStart() {
		return _tokenStart;
	}

	@Nullable
	@Override
	public IElementType getTokenType() {
		return _tokenType;
	}

	@Override
	public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
		_buffer = buffer;
		_endOffset = endOffset;
		_tokenStart = _tokenEnd = startOffset;
		_defaultState = initialState == 0;

		_parseNextToken();
	}

	private void _parseNextToken() {
		if (_tokenStart >= _endOffset) {
			_tokenType = null;
			_tokenEnd = _tokenStart;

			return;
		}

		boolean atLineStart = false;

		if ((_tokenStart == 0) || (_buffer.charAt(_tokenStart - 1) == '\n')) {
			atLineStart = true;
		}

		char c1 = _buffer.charAt(_tokenStart);

		if (_tokenStart > 1) {
			int c2 = _buffer.charAt(_tokenStart - 2);
			int c3 = _buffer.charAt(_tokenStart - 1);

			if ((c2 == '\\') && (c3 == '\n')) {
				atLineStart = false;
			}
		}

		if (atLineStart) {
			_defaultState = true;

			if (c1 == ' ') {
				_tokenType = TokenType.WHITE_SPACE;
				_tokenEnd = _tokenStart + 1;
			}
			else if (c1 == '\n') {
				_tokenType = BndTokenType.SECTION_END;
				_tokenEnd = _tokenStart + 1;
			}
			else if (c1 == '#') {
				int commentEnd = _tokenStart + 1;

				while (commentEnd < _endOffset) {
					c1 = _buffer.charAt(commentEnd);

					if (c1 == '\n') {
						break;
					}

					++commentEnd;
				}

				_tokenType = BndTokenType.COMMENT;
				_tokenEnd = commentEnd;
			}
			else {
				int headerEnd = _tokenStart + 1;

				while (headerEnd < _endOffset) {
					c1 = _buffer.charAt(headerEnd);

					if (c1 == ':') {
						_defaultState = false;

						break;
					}
					else if (c1 == '\n') {
						break;
					}

					++headerEnd;
				}

				_tokenType = BndTokenType.HEADER_NAME;
				_tokenEnd = headerEnd;
			}
		}
		else if (!_defaultState && (c1 == ':')) {
			_tokenType = BndTokenType.COLON;
			_tokenEnd = _tokenStart + 1;
		}
		else if (!_defaultState && (c1 == ' ')) {
			_tokenType = TokenType.WHITE_SPACE;
			_tokenEnd = _tokenStart + 1;
			_defaultState = true;
		}
		else {
			_defaultState = true;
			IElementType special;

			if (c1 == '\n') {
				if (_tokenType == BndTokenType.COMMENT) {

					// end of comment

					_tokenType = BndTokenType.SECTION_END;
					_tokenEnd = _tokenStart + 1;
				}
				else {
					_tokenType = BndTokenType.NEWLINE;
					_tokenEnd = _tokenStart + 1;
				}
			}
			else if ((special = _specialCharactersTokenMap.get(c1)) != null) {
				_tokenType = special;
				_tokenEnd = _tokenStart + 1;
			}
			else if (c1 == ' ') {
				_tokenType = TokenType.WHITE_SPACE;
				_tokenEnd = _tokenStart + 1;
			}
			else {
				int valueEnd = _tokenStart + 1;

				while (valueEnd < _endOffset) {
					c1 = _buffer.charAt(valueEnd);

					if ((c1 == '\n') || _specialCharactersTokenMap.containsKey(c1)) {
						break;
					}

					++valueEnd;
				}

				_tokenType = BndTokenType.HEADER_VALUE_PART;
				_tokenEnd = valueEnd;
			}
		}
	}

	private static final Map<Character, IElementType> _specialCharactersTokenMap =
		new HashMap<Character, IElementType>() {
			{
				put('(', BndTokenType.OPENING_PARENTHESIS_TOKEN);
				put(')', BndTokenType.CLOSING_PARENTHESIS_TOKEN);
				put(',', BndTokenType.COMMA);
				put(':', BndTokenType.COLON);
				put(';', BndTokenType.SEMICOLON);
				put('=', BndTokenType.EQUALS);
				put('[', BndTokenType.OPENING_BRACKET_TOKEN);
				put('\"', BndTokenType.QUOTE);
				put('\\', BndTokenType.BACKSLASH_TOKEN);
				put(']', BndTokenType.CLOSING_BRACKET_TOKEN);
			}
		};

	private CharSequence _buffer;
	private boolean _defaultState;
	private int _endOffset;
	private int _tokenEnd;
	private int _tokenStart;
	private IElementType _tokenType;

}