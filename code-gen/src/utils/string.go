package utils

import (
	"bufio"
	"bytes"
	"strings"
)

func TextLineMapper(text string, mapper func(line string, lineNumber int, totalLine int) (t string, stop bool)) (string, error) {
	if mapper == nil {
		return text, nil
	}

	totalLine := CountLines(text)

	stop := false
	line := ""
	index := 0
	newText := strings.Builder{}
	s := bufio.NewScanner(bytes.NewReader([]byte(text)))
	for s.Scan() {
		line, stop = mapper(s.Text(), index, totalLine)
		if index > 0 {
			line = "\n" + line
		}

		if _, err := newText.WriteString(line); err != nil {
			return "", err
		}

		if stop {
			break
		}

		index++
	}

	return newText.String(), nil
}

func CountLines(text string) int {
	lines := 0
	for _, char := range text {
		if char == '\n' {
			lines++
		}
	}

	// 如果字符串不以换行符结尾，则需要额外加一
	if lines > 0 && text[len(text)-1] != '\n' {
		lines++
	}
	return lines
}
