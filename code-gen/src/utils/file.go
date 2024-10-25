package utils

import (
	"os"
)

func ExistDir(dir string) (bool, error) {
	stat, err := os.Stat(dir)
	if err != nil {
		if os.IsNotExist(err) {
			return false, nil
		}

		return false, err
	}
	return stat.IsDir(), nil
}

func ExistFile(dir string) (bool, error) {
	stat, err := os.Stat(dir)
	if err != nil {
		if os.IsNotExist(err) {
			return false, nil
		}

		return false, err
	}
	return !stat.IsDir(), nil
}
