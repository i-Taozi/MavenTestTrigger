-- MySQL dump 10.13  Distrib 5.1.45, for Win64 (unknown)
--
-- Host: localhost    Database: nuodbtest
-- ------------------------------------------------------
-- Server version	5.1.45-community

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `datatypes1`
--

DROP TABLE IF EXISTS `datatypes1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datatypes1` (
  `c1` varchar(20) DEFAULT NULL,
  `c2` tinyint(4) DEFAULT NULL,
  `c3` text,
  `c4` date DEFAULT NULL,
  `c5` smallint(6) DEFAULT NULL,
  `c6` mediumint(9) NOT NULL DEFAULT '0',
  `c7` int(11) DEFAULT NULL,
  `c8` bigint(20) DEFAULT NULL,
  `c9` float(10,2) DEFAULT NULL,
  `c10` double DEFAULT NULL,
  `c11` bit,
  `c12` varbinary(90),
  `c13` binary(90),
  `c14` tinyblob,
 -- `c15` serial, // DB-24133
  PRIMARY KEY (`c6`),
  UNIQUE KEY `c2` (`c2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Dumping data for table `datatypes1`
--

LOCK TABLES `datatypes1` WRITE;
/*!40000 ALTER TABLE `datatypes1` DISABLE KEYS */;
-- INSERT INTO `datatypes1` VALUES ('test1',23,'sample text value','2012-09-29',45,345,67,8767,243.34,345.455,1,'^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk','^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk','^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk',10),('test 2',83,'','1995-03-19',454,3445,97,876765,123.54,235.565,0,'^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk','^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk','^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk',11);
INSERT INTO `datatypes1` VALUES ('test1',23,'sample text value','2012-09-29',45,345,67,8767,243.34,345.455,1,'^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk','^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk','^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk'),('test 2',83,'','1995-03-19',454,3445,97,876765,123.54,235.565,0,'^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk','^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk','^W.I_���<~D�x?E�&^��\r^\^�3F\r\nSo�WloMoOuwu�0�%?.T�r�z_}�_^X#!c�|�v�^\�n��^X�^F%.x~�x-Sk');
/*!40000 ALTER TABLE `datatypes1` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER lastmodtrigger BEFORE INSERT ON `datatypes1` FOR EACH ROW SET NEW.c4 = NOW() */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `datatypes2`
--

DROP TABLE IF EXISTS `datatypes2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datatypes2` (
  `k1` int(10) NOT NULL AUTO_INCREMENT,
  `c1` decimal(10,2) DEFAULT NULL,
  `c2` datetime DEFAULT NULL,
  `c3` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `c4` year(4) DEFAULT NULL,
  `c5` char(20) NOT NULL,
  `c6` ENUM('abcd', 'check', 'sample test') DEFAULT NULL,
  `c7` SET('one', 'two', '','three') DEFAULT NULL,
  PRIMARY KEY (`k1`),
  KEY `idx_c5` (`c5`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datatypes2`
--

LOCK TABLES `datatypes2` WRITE;
/*!40000 ALTER TABLE `datatypes2` DISABLE KEYS */;
INSERT INTO `datatypes2` VALUES (1,'345.23','1986-12-29 23:45:59','1986-12-29 07:29:59',2012,'12345678900987654321','check',''),(2,'125.63','2000-10-19 23:45:59','2000-10-19 03:19:49',2013,'abcd','sample test','two');
/*!40000 ALTER TABLE `datatypes2` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `datatypes3`
--

DROP TABLE IF EXISTS `datatypes3`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datatypes3` (
  `fk1` int(10) NOT NULL,
  `c1` tinytext,
  `c2` blob,
  `c3` mediumblob,
  `c4` mediumtext,
  `c5` longblob,
  `c6` longtext,
  KEY `idx_fk1` (`fk1`),
  CONSTRAINT `datatypes3_ibfk_1` FOREIGN KEY (`fk1`) REFERENCES `datatypes2` (`k1`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datatypes3`
--

LOCK TABLES `datatypes3` WRITE;
/*!40000 ALTER TABLE `datatypes3` DISABLE KEYS */;
INSERT INTO `datatypes3` VALUES (1,'nuodb','�PNG\r\n\Z\n\0\0\0\rIHDR\0\0\0�\0\0\0Z\0\0\0nRn1\0\0\0tEXtSoftware\0Adobe ImageReadyq�e<\0\0\n�IDATx��]M�E�]fQ~�6�H�F%�D���@���ă�7o/v�y��7��.�޼��<���x���&^��\r���F\r\n���WloM�Ouwu�0�%�����z_}��#!��|�v��n����.x~��~-Sk<�2e8����7��=�q��@_�,~�x�y}W	�g��q��]�f�@s��\'^�\r=�S|�aﮓt��x=/<J����\n�A��\Z���u<O)�qXp��h~�Ы�5[e�>MX|C��L����7]�B��f����V	�7Ww��&��ʹ^A��@�A�<��k*�/+�����u�9�����=(��JZC��l���{~]D�%؇��\"�siy)�i䍿�Fs$iz�g�yF��NQ�[X����2�8h������?]�����QGx$�n�%���2q��^`�f�����i|��ο��~�,���>�����P��C����O�|���<\';x֐�c����f�~.���ʻ�4s1ў��\\�)&\'��2A��m$�q���`Ӥ\Z�\'4���}�[i\"�?6���/C�����;��1�xM�`��J�H�\0N\Z�1C�4Y�N\n��$�Id��p?g�i�Ď)�wmN����u�2� ��;7۰<!>�P���Ӂ2ާ��V�\0��*��Y��~4��*�F�t���M�C5���v����4�&b����Y�n�9Lo��=1N���:��Kƅ��<v��{�\n��:%DV*�t����3���a���:/�� ��FC��zO���z3C�*6U2x��PDU���?�u\08�5�|�vx�qj�8J֩��I1N���b�\Z\0\'�����3�:�zH�F�S7� �<M��p�(��ޑ�����s\n\nz���B�������3��;������N���?V���2���=��AAO��,����94]Ñ�=������e&�E��N����N@�6�R��#����&��Vq(��1�G�����߲ï�!V!�����o��.zz��o���䉫H��}��y��g��oq��;g�����Z;gc���W�|2E*� ��\0S>��X`��/��p�����lv�p�.�`l�~΄�sه�����K�X��{�B��-�#�L2p���lMO���Ѐ�S�4j����pPĪ�6�\"Up\n7A$2A�������xEU%� O�+6�x$E�p\n6A�t��U\n\Z�g��4AQl�s`� ���&�؂�3b�,�5w�5�-8a��-HHHHHHHHH(����)Br�m%�D>f��\0�6�x>n<�K�<\'���ݚl+O%��..���X�]�,[��&��[�ݔ���~rw�P���}��\\��xiջi��G����=S��9x�mޚ$��N\'���5�/��]���\nz��@���2V$иl�*Wh���g�������)�8>�N�3\0��9�N���\r���a��{/����	�\\���)��8������;X�>m�و\0���\\A�X�����=���ʋu��2K)�[���M�oa-|�?��i���0�M�bḁ,�s�C~�<��C�y]���\'��B�G4�F]V��E�j��\\酻�s�	n�4�3�����/t�#5t��� l���XP���_�N�ކ�e���w\\E��B��dv��DG.�7�b\\��w�:�PH�f��S�ix�\'���4�w\\E��UlP����e��\r/�1Gd)��H6�\r�N00Ҁ����n�iɮmc\Z�`:��e�����������i!(\"*l����]Y<�zQQ^�`��U��0�w�o\n\'UMKέ�Ԃ���p��b�dߡ�%|G�ȍ� c3\0R�W$��� ل�_��ZD�a*�l��2^z�G���9��Q4x�`����Y�JСR�\'��f��U�9�H+�R�joC�ě��2X^[�?�γ**߭�yʲ\"-�>]�h7\0�g��(l���6���뀬�e�Q�a3b�=�V�Y~-K`�u��1��)r�Q_b4����$�pzNZߴ���$0�%1�z��,30��6h&8�Ȳ,@ӕZ\Z���e��g�*�mM#υ�N;�x��uEU��&(�t/n�JDUN�����@W�T�MJ�e\"!��y��E���\n�|�2�>��\0Rf��B�\n�H���N���01���_r�f�xF�p\"���h�G�\r$�k@�}�fJ��p�UeNxU�	\n�;씎���wp~NV�YV�7}C�Q͓rePW�Ⱦ@W�|��/H���#7���d�fn�zf\nbWRl��\'����8{s�4�7���nLy��(z���<SO���8:���ƅ��3\ne3��	���8x:\\1��N��%t��^$ͯ_��ۓ��u�E����z��)fn�Tޱ��ZL�`XV�<T���Z	f�^�jW(rxV&����@����t�:	�NY�E�,�p�4�.�r���0ht��{�����&� g���>��Khz\n�EV6x���G�FKyXf[��XL��.�[\Z?h�\n��]�y[.Ә�Sy8��l1���A3����0���\0M�)���@�a.<w+&��ӈ���~F$�1R�Ȋ���#ws\Z	hy�m����FN�`�ʘ/��S[�y�>4#�Q�dHR�\0&�u��n\0\0\0\0IEND�B`�','�PNG\r\n\Z\n\0\0\0\rIHDR\0\0\0�\0\0\0Z\0\0\0nRn1\0\0\0tEXtSoftware\0Adobe ImageReadyq�e<\0\0\n�IDATx��]M�E�]fQ~�6�H�F%�D���@���ă�7o/v�y��7��.�޼��<���x���&^��\r���F\r\n���WloM�Ouwu�0�%�����z_}��#!��|�v��n����.x~��~-Sk<�2e8����7��=�q��@_�,~�x�y}W	�g��q��]�f�@s��\'^�\r=�S|�aﮓt��x=/<J����\n�A��\Z���u<O)�qXp��h~�Ы�5[e�>MX|C��L����7]�B��f����V	�7Ww��&��ʹ^A��@�A�<��k*�/+�����u�9�����=(��JZC��l���{~]D�%؇��\"�siy)�i䍿�Fs$iz�g�yF��NQ�[X����2�8h������?]�����QGx$�n�%���2q��^`�f�����i|��ο��~�,���>�����P��C����O�|���<\';x֐�c����f�~.���ʻ�4s1ў��\\�)&\'��2A��m$�q���`Ӥ\Z�\'4���}�[i\"�?6���/C�����;��1�xM�`��J�H�\0N\Z�1C�4Y�N\n��$�Id��p?g�i�Ď)�wmN����u�2� ��;7۰<!>�P���Ӂ2ާ��V�\0��*��Y��~4��*�F�t���M�C5���v����4�&b����Y�n�9Lo��=1N���:��Kƅ��<v��{�\n��:%DV*�t����3���a���:/�� ��FC��zO���z3C�*6U2x��PDU���?�u\08�5�|�vx�qj�8J֩��I1N���b�\Z\0\'�����3�:�zH�F�S7� �<M��p�(��ޑ�����s\n\nz���B�������3��;������N���?V���2���=��AAO��,����94]Ñ�=������e&�E��N����N@�6�R��#����&��Vq(��1�G�����߲ï�!V!�����o��.zz��o���䉫H��}��y��g��oq��;g�����Z;gc���W�|2E*� ��\0S>��X`��/��p�����lv�p�.�`l�~΄�sه�����K�X��{�B��-�#�L2p���lMO���Ѐ�S�4j����pPĪ�6�\"Up\n7A$2A�������xEU%� O�+6�x$E�p\n6A�t��U\n\Z�g��4AQl�s`� ���&�؂�3b�,�5w�5�-8a��-HHHHHHHHH(����)Br�m%�D>f��\0�6�x>n<�K�<\'���ݚl+O%��..���X�]�,[��&��[�ݔ���~rw�P���}��\\��xiջi��G����=S��9x�mޚ$��N\'���5�/��]���\nz��@���2V$иl�*Wh���g�������)�8>�N�3\0��9�N���\r���a��{/����	�\\���)��8������;X�>m�و\0���\\A�X�����=���ʋu��2K)�[���M�oa-|�?��i���0�M�bḁ,�s�C~�<��C�y]���\'��B�G4�F]V��E�j��\\酻�s�	n�4�3�����/t�#5t��� l���XP���_�N�ކ�e���w\\E��B��dv��DG.�7�b\\��w�:�PH�f��S�ix�\'���4�w\\E��UlP����e��\r/�1Gd)��H6�\r�N00Ҁ����n�iɮmc\Z�`:��e�����������i!(\"*l����]Y<�zQQ^�`��U��0�w�o\n\'UMKέ�Ԃ���p��b�dߡ�%|G�ȍ� c3\0R�W$��� ل�_��ZD�a*�l��2^z�G���9��Q4x�`����Y�JСR�\'��f��U�9�H+�R�joC�ě��2X^[�?�γ**߭�yʲ\"-�>]�h7\0�g��(l���6���뀬�e�Q�a3b�=�V�Y~-K`�u��1��)r�Q_b4����$�pzNZߴ���$0�%1�z��,30��6h&8�Ȳ,@ӕZ\Z���e��g�*�mM#υ�N;�x��uEU��&(�t/n�JDUN�����@W�T�MJ�e\"!��y��E���\n�|�2�>��\0Rf��B�\n�H���N���01���_r�f�xF�p\"���h�G�\r$�k@�}�fJ��p�UeNxU�	\n�;씎���wp~NV�YV�7}C�Q͓rePW�Ⱦ@W�|��/H���#7���d�fn�zf\nbWRl��\'����8{s�4�7���nLy��(z���<SO���8:���ƅ��3\ne3��	���8x:\\1��N��%t��^$ͯ_��ۓ��u�E����z��)fn�Tޱ��ZL�`XV�<T���Z	f�^�jW(rxV&����@����t�:	�NY�E�,�p�4�.�r���0ht��{�����&� g���>��Khz\n�EV6x���G�FKyXf[��XL��.�[\Z?h�\n��]�y[.Ә�Sy8��l1���A3����0���\0M�)���@�a.<w+&��ӈ���~F$�1R�Ȋ���#ws\Z	hy�m����FN�`�ʘ/��S[�y�>4#�Q�dHR�\0&�u��n\0\0\0\0IEND�B`�','logo','�PNG\r\n\Z\n\0\0\0\rIHDR\0\0\0�\0\0\0Z\0\0\0nRn1\0\0\0tEXtSoftware\0Adobe ImageReadyq�e<\0\0\n�IDATx��]M�E�]fQ~�6�H�F%�D���@���ă�7o/v�y��7��.�޼��<���x���&^��\r���F\r\n���WloM�Ouwu�0�%�����z_}��#!��|�v��n����.x~��~-Sk<�2e8����7��=�q��@_�,~�x�y}W	�g��q��]�f�@s��\'^�\r=�S|�aﮓt��x=/<J����\n�A��\Z���u<O)�qXp��h~�Ы�5[e�>MX|C��L����7]�B��f����V	�7Ww��&��ʹ^A��@�A�<��k*�/+�����u�9�����=(��JZC��l���{~]D�%؇��\"�siy)�i䍿�Fs$iz�g�yF��NQ�[X����2�8h������?]�����QGx$�n�%���2q��^`�f�����i|��ο��~�,���>�����P��C����O�|���<\';x֐�c����f�~.���ʻ�4s1ў��\\�)&\'��2A��m$�q���`Ӥ\Z�\'4���}�[i\"�?6���/C�����;��1�xM�`��J�H�\0N\Z�1C�4Y�N\n��$�Id��p?g�i�Ď)�wmN����u�2� ��;7۰<!>�P���Ӂ2ާ��V�\0��*��Y��~4��*�F�t���M�C5���v����4�&b����Y�n�9Lo��=1N���:��Kƅ��<v��{�\n��:%DV*�t����3���a���:/�� ��FC��zO���z3C�*6U2x��PDU���?�u\08�5�|�vx�qj�8J֩��I1N���b�\Z\0\'�����3�:�zH�F�S7� �<M��p�(��ޑ�����s\n\nz���B�������3��;������N���?V���2���=��AAO��,����94]Ñ�=������e&�E��N����N@�6�R��#����&��Vq(��1�G�����߲ï�!V!�����o��.zz��o���䉫H��}��y��g��oq��;g�����Z;gc���W�|2E*� ��\0S>��X`��/��p�����lv�p�.�`l�~΄�sه�����K�X��{�B��-�#�L2p���lMO���Ѐ�S�4j����pPĪ�6�\"Up\n7A$2A�������xEU%� O�+6�x$E�p\n6A�t��U\n\Z�g��4AQl�s`� ���&�؂�3b�,�5w�5�-8a��-HHHHHHHHH(����)Br�m%�D>f��\0�6�x>n<�K�<\'���ݚl+O%��..���X�]�,[��&��[�ݔ���~rw�P���}��\\��xiջi��G����=S��9x�mޚ$��N\'���5�/��]���\nz��@���2V$иl�*Wh���g�������)�8>�N�3\0��9�N���\r���a��{/����	�\\���)��8������;X�>m�و\0���\\A�X�����=���ʋu��2K)�[���M�oa-|�?��i���0�M�bḁ,�s�C~�<��C�y]���\'��B�G4�F]V��E�j��\\酻�s�	n�4�3�����/t�#5t��� l���XP���_�N�ކ�e���w\\E��B��dv��DG.�7�b\\��w�:�PH�f��S�ix�\'���4�w\\E��UlP����e��\r/�1Gd)��H6�\r�N00Ҁ����n�iɮmc\Z�`:��e�����������i!(\"*l����]Y<�zQQ^�`��U��0�w�o\n\'UMKέ�Ԃ���p��b�dߡ�%|G�ȍ� c3\0R�W$��� ل�_��ZD�a*�l��2^z�G���9��Q4x�`����Y�JСR�\'��f��U�9�H+�R�joC�ě��2X^[�?�γ**߭�yʲ\"-�>]�h7\0�g��(l���6���뀬�e�Q�a3b�=�V�Y~-K`�u��1��)r�Q_b4����$�pzNZߴ���$0�%1�z��,30��6h&8�Ȳ,@ӕZ\Z���e��g�*�mM#υ�N;�x��uEU��&(�t/n�JDUN�����@W�T�MJ�e\"!��y��E���\n�|�2�>��\0Rf��B�\n�H���N���01���_r�f�xF�p\"���h�G�\r$�k@�}�fJ��p�UeNxU�	\n�;씎���wp~NV�YV�7}C�Q͓rePW�Ⱦ@W�|��/H���#7���d�fn�zf\nbWRl��\'����8{s�4�7���nLy��(z���<SO���8:���ƅ��3\ne3��	���8x:\\1��N��%t��^$ͯ_��ۓ��u�E����z��)fn�Tޱ��ZL�`XV�<T���Z	f�^�jW(rxV&����@����t�:	�NY�E�,�p�4�.�r���0ht��{�����&� g���>��Khz\n�EV6x���G�FKyXf[��XL��.�[\Z?h�\n��]�y[.Ә�Sy8��l1���A3����0���\0M�)���@�a.<w+&��ӈ���~F$�1R�Ȋ���#ws\Z	hy�m����FN�`�ʘ/��S[�y�>4#�Q�dHR�\0&�u��n\0\0\0\0IEND�B`�','image binary');
/*!40000 ALTER TABLE `datatypes3` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary table structure for view `datatypesview`
--

DROP TABLE IF EXISTS `datatypesview`;
/*!50001 DROP VIEW IF EXISTS `datatypesview`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `datatypesview` (
  `d2c1` decimal(10,2),
  `d3c1` tinytext
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `datatypesview`
--

/*!50001 DROP TABLE IF EXISTS `datatypesview`*/;
/*!50001 DROP VIEW IF EXISTS `datatypesview`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `datatypesview` AS select `d2`.`c1` AS `d2c1`,`d3`.`c1` AS `d3c1` from (`datatypes2` `d2` join `datatypes3` `d3`) where (`d2`.`k1` = `d3`.`fk1`) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-03-03  0:49:36
